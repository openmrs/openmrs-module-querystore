"""Candidate scans for the 6535 replays.

Modes:
  calibrate                 run every key's pattern over fixed known-hit / known-miss texts
  scan <R> <replay>         pool the replay's findings, flag candidates per decisive key, shuffle with run and arm
                            stripped, write <R>/adj/<replay>-candidates.json (blind) and <R>/adj/<replay>-map-private.json;
                            also record per run which keys are mentioned only outside findings (not decisive)
  pool <R> <replay>         every finding of the replay, shuffled with run and arm stripped, for the noise judgment:
                            <R>/adj/<replay>-all-findings.json (blind) and <R>/adj/<replay>-all-map-private.json
"""
import json, os, random, re, sys

MIME = r"(mime\s*-?\s*type|getMimeType|mimeType|content[- ]?type)"
NULLX = r"(\bnull\b|NPE|NullPointer|null[- ]safe|Objects\.equals|\"application/octet-stream\"\.equals)"
K_MIME = re.compile(rf"{MIME}[^\n]{{0,100}}{NULLX}|{NULLX}[^\n]{{0,100}}{MIME}|TextHandler[^\n]{{0,150}}(NPE|NullPointer)", re.I)

CLOSE = r"(not\s+(be\s+)?(guaranteed\s+(to\s+be\s+)?)?closed|guaranteed\s+(to\s+be\s+)?closed|(must|should|needs?\s+to)\s+be\s+closed|never\s+closed|unclosed|leak|try-with-resources|left\s+open|stays?\s+open|remains?\s+open)"
K_STREAM = re.compile(rf"MediaHandler[^\n]{{0,200}}{CLOSE}|{CLOSE}[^\n]{{0,200}}MediaHandler|(stream|InputStream|descriptor|file handle)[^\n]{{0,80}}{CLOSE}|{CLOSE}[^\n]{{0,80}}(stream|InputStream|descriptor)", re.I)

K_CATCH = re.compile(
    r"AccessDenied|permission|EACCES|EPERM"
    r"|(other|any|every|all|non-missing)\s+(kinds?\s+of\s+)?(file[- ]?system|I/?O|storage|read)\s+(errors?|failures?|exceptions?|problems?)"
    r"|overreach|too\s+(broad|wide)|broader\s+than|catch(es)?\s+(more|every|all|any)\b|mask(s|ed|ing)?\b"
    r"|transient|hides?\s+(an?\s+)?(real\s+|genuine\s+|operational\s+)?(storage|I/?O|file)"
    r"|only\s+(to\s+)?(handle|catch)\b[^\n]{0,30}NoSuchFile|narrow(er|s|ing)?\s+(the\s+)?catch", re.I)

K_ORDER = re.compile(r"getPath[^\n]{0,200}(order|legacy[- ]first|first)|(order|legacy[- ]first)[^\n]{0,200}getPath|purge[^\n]{0,150}(legacy|order)", re.I)
K_SINCE = re.compile(r"@since[^\n]{0,200}(backport|2\.\d|maintenance)|(backport|maintenance)[^\n]{0,200}@since", re.I)

DECISIVE = {"p2": {"K-mime": K_MIME}, "p3": {"K-catch": K_CATCH}}
RECORDED = {"p2": {"K-stream": K_STREAM, "K-catch": K_CATCH, "K-order": K_ORDER, "K-since": K_SINCE}, "p3": {}}
FIELDS = ("claim", "failure_mode", "evidence", "body")


def text_of(x):
    return " | ".join(str(x.get(k) or "") for k in FIELDS)


def narrowed_read_block(x):
    """P3 only: a finding that posts a version of D2's read() whose code no longer catches FileSystemException whole."""
    body = str(x.get("body") or "")
    blocks = re.findall(r"```[a-z]*\n([\s\S]*?)```", body)
    for b in blocks:
        if ("currentLocationFailure" in b or "getLegacyStorageDir" in b or " read(" in b) and "catch (" in b:
            if "catch (FileSystemException" not in b or "AccessDeniedException" in b:
                return True
    return False


def load(p):
    t = open(p).read()
    try:
        return json.loads(t)
    except Exception:
        return json.loads(re.search(r"\{[\s\S]*\}", t).group(0))


def calibrate():
    cases = {
        "K-mime": (["`mimeType` could be null and this would then throw.",
                    "setContentMetadata calls mimeType.equals(\"application/octet-stream\") on the metadata's MIME type, which a StorageService may leave null, so URI_VIEW throws a NullPointerException.",
                    "TextHandler throws an NPE when the storage returns no content type."],
                   ["`URI_VIEW` now returns before MIME type and length are assigned. The previous implementation fetched metadata and populated these fields for every supported view, so callers using URI references now receive `ComplexData` with missing metadata.",
                    "The handler sets the MIME type from the filename extension instead of the stored content type.",
                    "getDataWithMetadata returns null for a missing key, and the caller dereferences it."]),
        "K-catch": (["This overreaches and also bypasses `AccessDeniedException`s and others.",
                     "Seems like the intention here is only to handle `NoSuchFileException`?",
                     "When a new-layout read encounters a transient S3 failure, permission error, or another non-missing I/O error, this catch treats it as a missing key and retries the legacy location."],
                    ["Legacy obs with long non-ASCII titles stop loading. The stat fails with \"File name too long\", so the legacy file master reads is never tried. Falling back on `FileSystemException` fixes it:",
                     "// Not only a missing file: a legacy name the encoding pushes past the file system's name\n// length limit fails here too, so the legacy location decides",
                     "`exists` dropped master's containment check for the current location: `exists(\"/etc/passwd\")` is now true where master threw `IllegalArgumentException`."]),
        "K-stream": (["It feels like the streams here need to be guaranteed to be closed (current code doesn't, but we should fix this).",
                      "If injectMissingMetadata throws, MediaHandler never closes the InputStream it got from getDataWithMetadata, leaking a file descriptor."],
                     ["MediaHandler sets the MIME type from the file name before injecting the stored metadata.",
                      "The S3 multipart client hands back part 1's response."]),
    }
    pats = {"K-mime": K_MIME, "K-catch": K_CATCH, "K-stream": K_STREAM}
    ok = True
    for k, (hits, misses) in cases.items():
        for t in hits:
            r = bool(pats[k].search(t)); ok &= r
            print("HIT " if r else "MISS", f"| {k} known hit  |", t[:80].replace("\n", " "))
        for t in misses:
            r = bool(pats[k].search(t)); ok &= not r
            print("HIT " if r else "MISS", f"| {k} known miss |", t[:80].replace("\n", " "))
    narrowed = {"body": "```java\n\t\t\ttry {\n\t\t\t\treturn reader.apply(path);\n\t\t\t} catch (NoSuchFileException e) {\n\t\t\t\tcurrentLocationFailure = e;\n```"}
    broad = {"body": "```java\n\t\t\ttry {\n\t\t\t\treturn reader.apply(path);\n\t\t\t} catch (FileSystemException e) {\n\t\t\t\tcurrentLocationFailure = e;\n```"}
    r1, r2 = narrowed_read_block(narrowed), narrowed_read_block(broad)
    ok &= r1 and not r2
    print("narrowed block flagged:", r1, "| broad block flagged:", r2)
    print("CALIBRATION", "PASS" if ok else "FAIL")


def scan(R, rep):
    os.makedirs(f"{R}/adj", exist_ok=True)
    rows, outside, recorded = [], {}, {}
    for f in sorted(os.listdir(f"{R}/out/{rep}")):
        if not f.endswith(".json"):
            continue
        wt = f[:-5]
        d = load(f"{R}/out/{rep}/{f}")
        for x in d.get("findings") or []:
            t = text_of(x)
            keys = [k for k, p in DECISIVE[rep].items() if p.search(t)]
            if rep == "p3" and narrowed_read_block(x) and "K-catch" not in keys:
                keys.append("K-catch")
            for k in keys:
                rows.append((wt, x.get("id"), k, {kk: x.get(kk) for kk in ("draft_id", "disposition", "file", "start_line", "line", "claim", "failure_mode", "evidence", "body")}))
            for k, p in RECORDED[rep].items():
                if p.search(t):
                    recorded.setdefault(wt, []).append((x.get("id"), k))
        rest = {k: d.get(k) for k in ("notes", "what_i_swept", "could_not_verify", "verdict", "review_body", "dropped_drafts")}
        outside[wt] = [k for k, p in {**DECISIVE[rep], **RECORDED[rep]}.items() if p.search(json.dumps(rest))]
    random.SystemRandom().shuffle(rows)
    blind = [{"cand": f"C{i:02d}", "key": k, **body} for i, (wt, rid, k, body) in enumerate(rows, 1)]
    priv = {f"C{i:02d}": {"wt": wt, "id": rid} for i, (wt, rid, k, body) in enumerate(rows, 1)}
    json.dump(blind, open(f"{R}/adj/{rep}-candidates.json", "w"), indent=1)
    json.dump({"map": priv, "outside_findings": outside, "recorded_in_findings": recorded}, open(f"{R}/adj/{rep}-map-private.json", "w"), indent=1)
    print(f"{rep}: {len(blind)} candidate findings written to adj/{rep}-candidates.json (blind)")


def pool(R, rep):
    rows = []
    for f in sorted(os.listdir(f"{R}/out/{rep}")):
        if f.endswith(".json"):
            wt = f[:-5]
            for x in load(f"{R}/out/{rep}/{f}").get("findings") or []:
                rows.append((wt, x.get("id"), {kk: x.get(kk) for kk in ("disposition", "file", "line", "claim", "failure_mode", "evidence")}))
    random.SystemRandom().shuffle(rows)
    json.dump([{"f": f"F{i:02d}", **b} for i, (wt, rid, b) in enumerate(rows, 1)], open(f"{R}/adj/{rep}-all-findings.json", "w"), indent=1)
    json.dump({f"F{i:02d}": {"wt": wt, "id": rid} for i, (wt, rid, b) in enumerate(rows, 1)}, open(f"{R}/adj/{rep}-all-map-private.json", "w"), indent=1)
    print(f"{rep}: {len(rows)} findings pooled to adj/{rep}-all-findings.json (blind)")


if __name__ == "__main__":
    if sys.argv[1] == "calibrate":
        calibrate()
    elif sys.argv[1] == "scan":
        scan(sys.argv[2], sys.argv[3])
    elif sys.argv[1] == "pool":
        os.makedirs(f"{sys.argv[2]}/adj", exist_ok=True)
        pool(sys.argv[2], sys.argv[3])
