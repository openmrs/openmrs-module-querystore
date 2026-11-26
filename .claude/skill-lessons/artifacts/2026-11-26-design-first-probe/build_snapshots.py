"""Build the two cases' snapshot folders as they stood at each round's start. Network reads: Jira REST (public) and gh."""
import json, os, shutil, subprocess, urllib.request
from datetime import datetime, timezone
PD = os.path.dirname(os.path.abspath(__file__))
OLD = os.path.join(os.path.dirname(PD), "probe85", "snapshots")
T85 = datetime(2026, 11, 24, 21, 31, tzinfo=timezone.utc)
T79 = datetime(2026, 11, 24, 21, 25, 7, tzinfo=timezone.utc)
FIELDS = "summary,description,status,comment,labels,fixVersions,parent,issuelinks"

def ts(s):
    s = s.replace("Z", "+00:00")
    if len(s) > 6 and s[-5] in "+-" and s[-3] != ":":
        s = s[:-2] + ":" + s[-2:]
    return datetime.fromisoformat(s)

def jira(key, fields, expand=""):
    url = f"https://openmrs.atlassian.net/rest/api/2/issue/{key}?fields={fields}" + (f"&expand={expand}" if expand else "")
    with urllib.request.urlopen(url, timeout=60) as r:
        assert r.status == 200, (key, r.status)
        return json.load(r)

def ticket_at(key, T, fields=FIELDS):
    d = jira(key, fields, "changelog")
    f = d["fields"]
    # status at T: walk status changes after T backwards
    status = f["status"]["name"]
    for h in sorted(d["changelog"]["histories"], key=lambda h: ts(h["created"]), reverse=True):
        if ts(h["created"]) <= T:
            break
        for it in h["items"]:
            if it["field"] == "status":
                status = it["fromString"]
        for it in h["items"]:
            assert it["field"] not in ("description", "summary", "Link", "IssueParentAssociation"), (key, it["field"], h["created"])
    out = {"key": d["key"], "fields": {}}
    for k in fields.split(","):
        if k not in f:
            continue
        v = f[k]
        if k == "status":
            v = {"name": status}
        elif k == "comment":
            v = {"comments": [{"created": c["created"], "author": {"displayName": c["author"]["displayName"]}, "body": c["body"]}
                              for c in v["comments"] if ts(c["created"]) <= T]}
        elif k == "parent" and v:
            v = {"key": v["key"], "fields": {"summary": v["fields"]["summary"]}}
        elif k == "issuelinks":
            v = [{"type": {"name": l["type"]["name"], "inward": l["type"]["inward"], "outward": l["type"]["outward"]},
                  ("outwardIssue" if "outwardIssue" in l else "inwardIssue"):
                      {"key": (l.get("outwardIssue") or l["inwardIssue"])["key"],
                       "fields": {"summary": (l.get("outwardIssue") or l["inwardIssue"])["fields"]["summary"]}}} for l in v]
        out["fields"][k] = v
    return out

def gh(path):
    return json.loads(subprocess.run(["gh", "api", "--paginate", "--slurp", path], capture_output=True, text=True, check=True).stdout)

def flat(pages):
    return [x for p in pages for x in (p if isinstance(p, list) else [p])]

def conversation(n, T):
    base = f"repos/openmrs/openmrs-module-stockmanagement"
    ic = [c for c in flat(gh(f"{base}/issues/{n}/comments")) if ts(c["created_at"]) <= T]
    rc = [c for c in flat(gh(f"{base}/pulls/{n}/comments")) if ts(c["created_at"]) <= T]
    rv = [r for r in flat(gh(f"{base}/pulls/{n}/reviews")) if r.get("submitted_at") and ts(r["submitted_at"]) <= T]
    keep_c = ("id", "user", "created_at", "body", "path", "line", "original_line", "side", "commit_id", "in_reply_to_id")
    slim = lambda c: {k: (c[k]["login"] if k == "user" else c[k]) for k in keep_c if k in c}
    return {"issue_comments": [slim(c) for c in ic], "review_comments": [slim(c) for c in rc],
            "reviews": [{"id": r["id"], "user": r["user"]["login"], "state": r["state"], "submitted_at": r["submitted_at"],
                         "commit_id": r["commit_id"], "body": r["body"]} for r in rv]}

def dump(obj, path):
    with open(path, "w") as fh:
        json.dump(obj, fh, indent=1, ensure_ascii=False)

# ---- case 85
s85 = os.path.join(PD, "snap85")
for f in ("pr85-body-at-review.md", "pr85-conversation-at-review.json", "agent-report-whole.md", "agent-report-c14.md",
          "agent-report-c2.md", "agent-report-c3.md"):
    shutil.copy(os.path.join(OLD, f), os.path.join(s85, f))
dump(ticket_at("O3-6352", T85), os.path.join(s85, "o3-6352-at-review.json"))
dump(ticket_at("O3-6306", T85, "summary,description,status,comment"), os.path.join(s85, "o3-6306-at-review.json"))
# ---- case 79
s79 = os.path.join(PD, "snap79")
pr = json.loads(subprocess.run(["gh", "pr", "view", "79", "--repo", "openmrs/openmrs-module-stockmanagement", "--json",
                                "body,title,lastEditedAt"], capture_output=True, text=True, check=True).stdout) if False else None
body = json.loads(subprocess.run(["gh", "api", "repos/openmrs/openmrs-module-stockmanagement/pulls/79"], capture_output=True,
                                 text=True, check=True).stdout)
open(os.path.join(s79, "pr79-body-at-review.md"), "w").write(body["body"] + "\n")
dump(conversation(79, T79), os.path.join(s79, "pr79-conversation-at-review.json"))
dump(ticket_at("O3-6340", T79), os.path.join(s79, "o3-6340-at-review.json"))
dump(ticket_at("O3-6306", T79, "summary,description,status,comment"), os.path.join(s79, "o3-6306-at-review.json"))
for n, dst in (("whole", "agent-report-whole.md"), ("ac1", "agent-report-ac1.md"), ("ac2", "agent-report-ac2.md"), ("ac3", "agent-report-ac3.md")):
    shutil.copy(os.path.expanduser(f"~/.claude/blind-runs/2026-11-25-sm79-r1-{n}/log/report.md"), os.path.join(s79, dst))
print("ok")
