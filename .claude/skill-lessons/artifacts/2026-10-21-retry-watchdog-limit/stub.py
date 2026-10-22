#!/usr/bin/env python3
"""A stand-in for the Messages API that answers every /v1/messages request with a claude.ai-style
usage-limit 429 until RESET_AT (epoch seconds), and with a one-word reply after it.

    stub.py <port> <reset_at_epoch> <log_path>

Every request is logged as one JSON line: when it arrived, the path, and the status returned, so a
run's wait can be read off the arrival times rather than off the CLI's own report.
"""
import json
import sys
import time
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

PORT, RESET_AT, LOG = int(sys.argv[1]), float(sys.argv[2]), sys.argv[3]
# "agent": the FIRST /v1/messages request is served before any limit, with a call to the
# agent tool, so the limit then lands on the agent's own request.
MODE = sys.argv[4] if len(sys.argv) > 4 else "plain"
SEEN = {"messages": 0}


def log(entry: dict) -> None:
    with open(LOG, "a") as fh:
        fh.write(json.dumps(entry) + "\n")


def sse(event: str, data: dict) -> bytes:
    return f"event: {event}\ndata: {json.dumps(data)}\n\n".encode()


class Handler(BaseHTTPRequestHandler):
    protocol_version = "HTTP/1.1"

    def log_message(self, *args):  # the JSON log above is the record; keep stderr quiet
        pass

    def do_GET(self):
        log({"t": time.time(), "method": "GET", "path": self.path, "status": 404})
        self._json(404, {"type": "error", "error": {"type": "not_found_error", "message": "stub"}})

    def do_POST(self):
        length = int(self.headers.get("content-length") or 0)
        body = self.rfile.read(length) if length else b""
        try:
            req = json.loads(body or b"{}")
        except ValueError:
            req = {}
        path = self.path.split("?")[0]
        now = time.time()
        if path.endswith("/count_tokens"):
            log({"t": now, "method": "POST", "path": path, "status": 200})
            return self._json(200, {"input_tokens": 10})
        if not path.endswith("/v1/messages"):
            log({"t": now, "method": "POST", "path": path, "status": 404})
            return self._json(404, {"type": "error", "error": {"type": "not_found_error",
                                                                "message": "stub"}})
        SEEN["messages"] += 1
        first = SEEN["messages"] == 1
        msgs = req.get("messages") or []
        last = msgs[-1].get("content") if msgs else ""
        if isinstance(last, list):
            last = " ".join(b.get("type", "") + ":" + str(b.get("text") or b.get("content") or "")[:40] for b in last)
        tools = [x.get("name") for x in (req.get("tools") or [])]
        who = {"n_messages": len(msgs), "last": str(last)[:80], "has_agent_tool": any(n in ("Agent", "Task") for n in tools)}
        if MODE == "agent" and first:
            name = next((n for n in tools if n in ("Agent", "Task")), None)
            log({"t": now, "method": "POST", "path": path, "status": 200, "served": "tool_use:" + str(name), **who})
            return self._tool_use(req, name)
        if now < RESET_AT:
            log({"t": now, "method": "POST", "path": path, "status": 429,
                 "stream": bool(req.get("stream")), "max_tokens": req.get("max_tokens"), **who})
            payload = json.dumps({"type": "error", "error": {
                "type": "rate_limit_error",
                "message": "This request would exceed your account's rate limit."}}).encode()
            self.send_response(429)
            self.send_header("content-type", "application/json")
            self.send_header("content-length", str(len(payload)))
            self.send_header("anthropic-ratelimit-unified-status", "rejected")
            self.send_header("anthropic-ratelimit-unified-reset", str(int(RESET_AT)))
            self.send_header("x-should-retry", "false")
            self.end_headers()
            self.wfile.write(payload)
            return
        log({"t": now, "method": "POST", "path": path, "status": 200,
             "stream": bool(req.get("stream")), "max_tokens": req.get("max_tokens"), **who})
        model = req.get("model") or "stub-model"
        if req.get("stream"):
            chunks = [
                sse("message_start", {"type": "message_start", "message": {
                    "id": "msg_stub", "type": "message", "role": "assistant", "model": model,
                    "content": [], "stop_reason": None, "stop_sequence": None,
                    "usage": {"input_tokens": 10, "output_tokens": 1}}}),
                sse("content_block_start", {"type": "content_block_start", "index": 0,
                                            "content_block": {"type": "text", "text": ""}}),
                sse("content_block_delta", {"type": "content_block_delta", "index": 0,
                                            "delta": {"type": "text_delta", "text": "STUB-OK"}}),
                sse("content_block_stop", {"type": "content_block_stop", "index": 0}),
                sse("message_delta", {"type": "message_delta",
                                      "delta": {"stop_reason": "end_turn", "stop_sequence": None},
                                      "usage": {"output_tokens": 2}}),
                sse("message_stop", {"type": "message_stop"}),
            ]
            data = b"".join(chunks)
            self.send_response(200)
            self.send_header("content-type", "text/event-stream")
            self.send_header("content-length", str(len(data)))
            self.end_headers()
            self.wfile.write(data)
            return
        self._json(200, {"id": "msg_stub", "type": "message", "role": "assistant", "model": model,
                         "content": [{"type": "text", "text": "STUB-OK"}],
                         "stop_reason": "end_turn", "stop_sequence": None,
                         "usage": {"input_tokens": 10, "output_tokens": 2}})

    def _tool_use(self, req: dict, name) -> None:
        model = req.get("model") or "stub-model"
        args = json.dumps({"description": "probe agent", "prompt": "Reply with the single word PONG.",
                           "subagent_type": "general-purpose"})
        chunks = [
            sse("message_start", {"type": "message_start", "message": {
                "id": "msg_stub_tool", "type": "message", "role": "assistant", "model": model,
                "content": [], "stop_reason": None, "stop_sequence": None,
                "usage": {"input_tokens": 10, "output_tokens": 1}}}),
            sse("content_block_start", {"type": "content_block_start", "index": 0, "content_block": {
                "type": "tool_use", "id": "toolu_stub_agent_1", "name": name or "Agent", "input": {}}}),
            sse("content_block_delta", {"type": "content_block_delta", "index": 0,
                                        "delta": {"type": "input_json_delta", "partial_json": args}}),
            sse("content_block_stop", {"type": "content_block_stop", "index": 0}),
            sse("message_delta", {"type": "message_delta",
                                  "delta": {"stop_reason": "tool_use", "stop_sequence": None},
                                  "usage": {"output_tokens": 20}}),
            sse("message_stop", {"type": "message_stop"}),
        ]
        data = b"".join(chunks)
        self.send_response(200)
        self.send_header("content-type", "text/event-stream")
        self.send_header("content-length", str(len(data)))
        self.end_headers()
        self.wfile.write(data)

    def _json(self, status: int, obj: dict) -> None:
        data = json.dumps(obj).encode()
        self.send_response(status)
        self.send_header("content-type", "application/json")
        self.send_header("content-length", str(len(data)))
        self.end_headers()
        self.wfile.write(data)


if __name__ == "__main__":
    ThreadingHTTPServer(("127.0.0.1", PORT), Handler).serve_forever()
