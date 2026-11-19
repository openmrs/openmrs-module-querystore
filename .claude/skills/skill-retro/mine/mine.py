#!/usr/bin/env python3
"""Mine past pr-review sessions for lessons: skill-retro's mining mode.

Stages run in order, and each skips work whose outputs already exist, so a run stopped by a usage
limit resumes where it stopped:
  scan      pr-review sessions with entries inside the window (since, until]
  corpus    date-ordered chunks: the owner's typed messages, each session's text, every subagent's
            brief and report (a background agent's report arrives as a task notification)
  measure   time to first report, context fill, compactions, refuter verdicts
  github    PR outcomes: threads, reactions, PRs closed unmerged, other reviewers' later comments
  memories  review-related project memories changed inside the window
  agents    headless readers, outcomes judges, memory readers, a skill auditor, synthesizers, and the
            two final mergers (skill text, machinery)
A run's folder (~/.claude/pr-review-mining/<date>/) holds window.json, its fixed window. With no
arguments the command resumes the newest unfinished run, else starts a new one at the watermark
~/.claude/pr-review-mining/MINED; `--out <folder>` resumes that folder's run, whatever started it. Only a
complete run that started from the watermark moves it, to the end of its window, and only while the watermark
still marks that start; a run given --since or --until (a test, a re-mine) leaves it alone.
The agents get Read, Grep and Glob only: they read untrusted text (PR comments, transcripts) and
must not be able to post, push or delete. Nothing here edits a skill.

`--self-test` checks each measurement against synthetic transcripts with known answers, including
the traps that once produced plausible wrong numbers. Run it after any edit to this file.
"""
import sys
sys.dont_write_bytecode = True
import argparse, collections, concurrent.futures as cf, contextlib, datetime as dt, glob, io, json, os, re
import shlex, shutil, subprocess, tempfile, time

HOME = os.path.expanduser('~')
PROJECTS = os.path.join(HOME, '.claude', 'projects')
MINE_ROOT = os.path.join(HOME, '.claude', 'pr-review-mining')
WATERMARK = os.path.join(MINE_ROOT, 'MINED')
PRS_FILE = os.path.join(HOME, '.claude', 'skill-lessons', 'confidential', 'PRS')
SKILLS = os.path.join(HOME, '.claude', 'skills')
LESSONS = os.path.join(HOME, '.claude', 'skill-lessons')
BRIEFS = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'briefs')

MARK = re.compile(r'^Base directory for this skill: \S*/skills/(?P<skill>[\w.-]+)')
ARGS = re.compile(r'Arguments: `(?P<a>[^`]*)`')
PULL = re.compile(r'github\.com/([\w.-]+)/([\w.-]+)/pull/(\d+)')
SKIP = ('base directory for this skill', '<command-', 'this session is being continued', '<task-notification>',
        'caveat:', '<local-command', '<system-reminder>', '[request interrupted')
REFUTER_BRIEF = re.compile(r'refut|adversar|try to break|break (it|this|these|each)|challenge each', re.I)
VERDICTS = [('REFUTED', r'\bREFUTED\b|\brefuted\b|\bRefuted\b'),
            ('WEAKENED', r'\bWEAKENED\b|\bweakened\b|\bWeakened\b|overstat'),
            ('HELD', r'\bSTANDS\b|\bHOLDS\b|\bholds\b|\bstands\b|\bsurvives\b'),
            ('WRONG', r"\bwrong\b|incorrect|does not hold|doesn.t hold|is false")]
REVIEWISH = re.compile(r'review|refut|reviewer|comment|post(ing|ed)? (on|to) (the )?(pr|pull)|maintainer|approv|nitpick', re.I)
ASYNC_ACK = 'Async agent launched'
READ_ONLY = ['Read', 'Grep', 'Glob']
DENY = ['Write', 'Edit', 'NotebookEdit', 'WebFetch', 'WebSearch', 'Agent', 'Task', 'Skill', 'Workflow']


def T(s):
    """An ISO time as an aware datetime. A time with no zone, such as `--since 2026-10-19`, is read as UTC,
    as the transcripts and the watermark are; read as local, it shifted the window and then failed to compare."""
    t = dt.datetime.fromisoformat(s.replace('Z', '+00:00'))
    return t if t.tzinfo else t.replace(tzinfo=dt.timezone.utc)


def whole(s):
    """An ISO time cut to the whole second: how window.json and the watermark store it."""
    return T(s).replace(microsecond=0)


def iso(t):
    return t.astimezone(dt.timezone.utc).strftime('%Y-%m-%dT%H:%M:%SZ')


def texts(content):
    if isinstance(content, str):
        return [content]
    return [x.get('text') or '' for x in content or [] if isinstance(x, dict) and x.get('type') == 'text']


def entries(path, until=None):
    """(index, entry) in file order, stopping at the first entry stamped after `until`."""
    with open(path, errors='replace') as fh:
        for i, line in enumerate(fh):
            try:
                d = json.loads(line)
            except Exception:
                continue
            if until and d.get('timestamp') and T(d['timestamp']) > until:
                return
            yield i, d


def typed_text(d):
    """The owner's typed text in a user entry, or None (tool results, meta notices, harness text)."""
    if d.get('type') != 'user' or d.get('isMeta'):
        return None
    c = (d.get('message') or {}).get('content')
    if isinstance(c, list) and any(isinstance(x, dict) and x.get('type') == 'tool_result' for x in c):
        return None
    t = ' '.join(s.strip() for s in texts(c) if s.strip())
    return t if t and not t.lower().startswith(SKIP) else None


# ---------------------------------------------------------------- scan
def scan_file(path):
    launches, first_ts, last_ts = [], None, None
    for i, d in entries(path):
        ts = d.get('timestamp')
        if ts:
            first_ts = first_ts or ts
            last_ts = ts
        if d.get('type') != 'user':
            continue
        for tx in texts((d.get('message') or {}).get('content')):
            m = MARK.match(tx)
            if m and m.group('skill') == 'pr-review':
                am = ARGS.search(tx[:3000])
                launches.append({'i': i, 'ts': ts, 'args': am.group('a') if am else ''})
                break
    return {'path': path, 'sid': os.path.basename(path)[:8], 'folder': os.path.basename(os.path.dirname(path)),
            'first_ts': first_ts, 'last_ts': last_ts, 'launches': launches}


def scan(since, until):
    """Main-session transcripts (not subagents) with a pr-review launch and an entry in (since, until]."""
    out = []
    for p in glob.glob(os.path.join(PROJECTS, '*', '*.jsonl')):
        if dt.datetime.fromtimestamp(os.path.getmtime(p), dt.timezone.utc) <= since:
            continue
        s = scan_file(p)
        if not s['launches'] or not s['last_ts'] or T(s['last_ts']) <= since:
            continue
        if T(s['launches'][0]['ts'] or s['first_ts']) > until or start_index(s, since, until) is None:
            continue
        out.append(s)
    out.sort(key=lambda s: s['first_ts'])
    return out


def start_index(s, since, until):
    """First entry to mine: at or after the first pr-review launch, inside (since, until]."""
    li = s['launches'][0]['i']
    for i, d in entries(s['path'], until):
        if i >= li and d.get('timestamp') and T(d['timestamp']) > since:
            return i
    return None


def launch_ts(s, since, until):
    """When this window's review began: the first launch inside the window, else the last launch before it."""
    ts = [la['ts'] for la in s['launches'] if la['ts']]
    inside = [x for x in ts if since < T(x) <= until]
    before = [x for x in ts if T(x) <= since]
    return inside[0] if inside else (before[-1] if before else ts[0])


# ---------------------------------------------------------------- private PRs
def load_private():
    """(repo, number) pairs from every pull URL in PRS, and the bare numbers, whatever else a line holds."""
    priv, nums = set(), set()
    if os.path.exists(PRS_FILE):
        for line in open(PRS_FILE):
            for m in PULL.finditer(line):
                priv.add((m.group(2), m.group(3)))
                nums.add(m.group(3))
    return priv, nums


def pr_of(s, block_text=''):
    """(owner, repo, n) from the launch arguments, else from the first pull URL the session names."""
    for la in s['launches']:
        m = PULL.search(la.get('args') or '')
        if m:
            return m.group(1), m.group(2), m.group(3)
    m = PULL.search(block_text)
    return (m.group(1), m.group(2), m.group(3)) if m else None


def is_private(s, key, private):
    priv, nums = private
    if key and (key[1], key[2]) in priv:
        return True
    # a bare number in the arguments, from any folder, is private if any private PR has that number
    bare = set(re.findall(r'(?:^|[\s#(])(\d{2,6})(?![\w/.-])', ' '.join(la.get('args') or '' for la in s['launches'])))
    return bool(bare & nums)


# ---------------------------------------------------------------- corpus
def wrap(s, w=900):
    out = []
    for para in s.split('\n'):
        while len(para) > w:
            k = para.rfind(' ', 0, w)
            k = k if k > 200 else w
            out.append(para[:k])
            para = para[k:].lstrip()
        out.append(para)
    return '\n'.join(out)


def session_block(s, since, until, private):
    """The session's mined text inside the window, and the counts the corpus control needs."""
    si = start_index(s, since, until)
    if si is None:
        return None, {}
    lines, agent_ids, stats = [], set(), collections.Counter()
    for i, d in entries(s['path'], until):
        if i < si:
            continue
        c = (d.get('message') or {}).get('content')
        ts = (d.get('timestamp') or '')[11:16]
        if d.get('type') == 'assistant' and isinstance(c, list):
            for x in c:
                if x.get('type') == 'text' and x['text'].strip():
                    lines.append(f'[{ts} A] ' + wrap(x['text'].strip()))
                if x.get('type') == 'tool_use' and x['name'] in ('Agent', 'Task'):
                    inp = x.get('input') or {}
                    agent_ids.add(x['id'])
                    lines.append(f"[{ts} AGENT-BRIEF] {inp.get('description', '')}: " + wrap((inp.get('prompt') or '')[:600].strip()))
        if d.get('type') == 'user':
            items = c if isinstance(c, list) else [{'type': 'text', 'text': c or ''}]
            for x in items:
                tx = x.get('text', '') if x.get('type') == 'text' else ''
                if tx.strip().startswith('<task-notification>'):
                    sm = re.search(r'<summary>(.*?)</summary>', tx, re.S)
                    rs = re.search(r'<result>(.*?)(</result>|$)', tx, re.S)
                    st = re.search(r'<status>(.*?)</status>', tx)
                    if rs and sm and sm.group(1).strip().startswith('Agent '):
                        stats['bg_results'] += 1
                    lines.append(f"[{ts} TASK-DONE] {(sm.group(1).strip() if sm else '')} ({st.group(1) if st else ''})"
                                 + ((': ' + wrap(rs.group(1).strip()[:4000])) if rs else ''))
                elif x.get('type') == 'text' and tx.strip() and not d.get('isMeta') and not tx.strip().lower().startswith(SKIP):
                    lines.append(f'[{ts} USER] ' + wrap(tx.strip()[:3000]))
                if x.get('type') == 'tool_result' and x.get('tool_use_id') in agent_ids:
                    r = x.get('content')
                    r = r if isinstance(r, str) else ''.join(y.get('text', '') for y in (r or []) if isinstance(y, dict))
                    if r.strip().startswith(ASYNC_ACK):
                        stats['async_launches'] += 1
                    else:
                        lines.append(f'[{ts} AGENT-REPORT] ' + wrap(r.strip()[:4000]))
    body = '\n'.join(lines)
    key = pr_of(s, body)
    priv = is_private(s, key, private)
    head = (f"=== SESSION {s['sid']} | folder {s['folder'].replace('-Users-' + os.path.basename(HOME), '~')} | PR "
            f"{(key[0] + '/' + key[1] + '#' + key[2]) if key else '(unresolved)'} | {s['first_ts'][:16]} -> {s['last_ts'][:16]} | "
            + ('PRIVATE PR: never name it in your output, call it a private PR' if priv else 'public'))
    return head + '\n' + body + '\n', {'key': key, 'private': priv, **stats}


def corpus_control(tot):
    """Background agents report through task notifications. A corpus holding few of their reports for
    the launches it saw has lost the refuters' verdicts, which once read as 0 refuter results of 401."""
    a, r = tot.get('async_launches', 0), tot.get('bg_results', 0)
    return not (a >= 5 and r < 0.3 * a), f'{a} background agent launches, {r} background reports'


def build_corpus(sessions, since, until, private, out, chunk_chars):
    os.makedirs(out, exist_ok=True)
    blocks, meta, tot = [], [], collections.Counter()
    for s in sessions:
        b, st = session_block(s, since, until, private)
        if b:
            blocks.append(b)
            meta.append({'sid': s['sid'], 'key': st.get('key'), 'private': st.get('private'), 'first_ts': s['first_ts'],
                         'launch_ts': launch_ts(s, since, until), 'folder': s['folder'], 'path': s['path']})
            tot.update({k: v for k, v in st.items() if isinstance(v, int)})
    ok, msg = corpus_control(tot)
    if not ok:
        raise SystemExit('corpus control failed: ' + msg)
    chunks, cur, size = [], [], 0
    for b in blocks:
        if cur and size + len(b) > chunk_chars:
            chunks.append(cur); cur, size = [], 0
        cur.append(b); size += len(b)
    if cur:
        chunks.append(cur)
    for k, c in enumerate(chunks, 1):
        open(os.path.join(out, f'chunk-{k:02d}.txt'), 'w').write('\n'.join(c))
    json.dump(meta, open(os.path.join(out, 'sessions.json'), 'w'), indent=1)
    json.dump({'chunks': len(chunks), 'sessions': len(blocks), **tot}, open(os.path.join(out, 'stats.json'), 'w'))
    return len(chunks), len(blocks), tot


# ---------------------------------------------------------------- measure
def timeline(s, since, until):
    """Seconds from the mined start to the first typed reply, split by what the session waited on."""
    si = start_index(s, since, until)
    seg, prev = collections.Counter(), None
    if si is None:
        return seg
    for i, d in entries(s['path'], until):
        if i < si:
            continue
        ts, t = d.get('timestamp'), d.get('type')
        if not ts or t not in ('assistant', 'user'):
            continue
        now, kind = T(ts), 'A'
        if t == 'user':
            c = (d.get('message') or {}).get('content')
            items = c if isinstance(c, list) else [{'type': 'text', 'text': c or ''}]
            tx = [x.get('text', '') for x in items if x.get('type') == 'text']
            if any(x.get('type') == 'tool_result' for x in items):
                kind = 'TR'
            elif any(x.strip().startswith('<task-notification>') for x in tx):
                kind = 'NOTIF'
            elif typed_text(d):
                kind = 'TYPED'
            else:
                kind = 'OTHER'
        if prev is not None:
            g = (now - prev).total_seconds()
            g = g if 0 <= g <= 12 * 3600 else 0
            if kind == 'TYPED':
                break
            seg[{'A': 'model', 'TR': 'foreground tools', 'NOTIF': 'background agents and tasks',
                 'OTHER': 'other (mostly background waits ended by compaction)'}[kind]] += g
        prev = now
    return seg


def tool_kind(name, inp):
    c = (inp.get('command') or '') if name == 'Bash' else ''
    if name != 'Bash':
        return name
    for k, p in (('gh', r'\bgh\b'), ('mvn', r'\bmvn\b|mvnw'), ('git diff/show/log', r'\bgit (diff|show|log)\b'),
                 ('cat/sed/head/tail', r'\b(cat|sed -n|head|tail)\b'), ('grep', r'\bgrep\b|\brg\b'), ('curl', r'\bcurl\b')):
        if re.search(p, c):
            return 'bash:' + k
    return 'bash:other'


def measure(sessions, since, until, corpus_dir, out):
    seg, per, fill, compactions, outtok = collections.Counter(), [], collections.Counter(), 0, []
    for s in sessions:
        sg = timeline(s, since, until)
        if sg:
            seg.update(sg); per.append(sum(sg.values()) / 60)
        si = start_index(s, since, until)
        if si is None:
            continue
        pend, seen, out_tokens = {}, set(), 0
        for i, d in entries(s['path'], until):
            if i < si:
                continue
            c = (d.get('message') or {}).get('content')
            if d.get('type') == 'assistant':
                mid = (d.get('message') or {}).get('id')
                if mid and mid not in seen:  # one response spans several lines, each repeating its usage
                    seen.add(mid); out_tokens += ((d.get('message') or {}).get('usage') or {}).get('output_tokens') or 0
                for x in c or []:
                    if isinstance(x, dict) and x.get('type') == 'tool_use':
                        pend[x['id']] = tool_kind(x['name'], x.get('input') or {})
            if d.get('type') == 'user':
                items = c if isinstance(c, list) else [{'type': 'text', 'text': c or ''}]
                for x in items:
                    if x.get('type') == 'tool_result' and x.get('tool_use_id') in pend:
                        r = x.get('content')
                        r = r if isinstance(r, str) else ''.join(y.get('text', '') for y in (r or []) if isinstance(y, dict))
                        fill[pend.pop(x['tool_use_id'])] += len(r)
                    if x.get('type') == 'text' and x.get('text', '').strip().lower().startswith('this session is being continued'):
                        compactions += 1
        outtok.append(out_tokens)
    pairs = refuter_pairs(corpus_dir)
    vc, signal = collections.Counter(), 0
    for _, b, r in pairs:
        hit = {k for k, p in VERDICTS if re.search(p, r)}
        vc.update(hit)
        signal += bool(hit & {'REFUTED', 'WEAKENED', 'WRONG'})
    per.sort(); W = sum(seg.values()) or 1; F = sum(fill.values()) or 1
    q = lambda a, f: a[min(len(a) - 1, int(len(a) * f))] if a else 0
    lines = ['# Measurements', '',
             f'- Sessions measured: {len(per)}. Minutes from the mined start to the first typed reply: median {q(per, .5):.0f}, p75 {q(per, .75):.0f}, p90 {q(per, .9):.0f}.',
             '- Where that time went: ' + '; '.join(f'{k} {100 * v / W:.0f}%' for k, v in seg.most_common()) + '.',
             f'- Compactions: {compactions}. Output tokens per session in the window (deduplicated by message id): median {q(sorted(outtok), .5) / 1000:.0f}k.',
             '- Context fill by tool output (characters): ' + '; '.join(f'{k} {100 * v / F:.0f}%' for k, v in fill.most_common(8)) + f'; total {F / 1e6:.0f}M.',
             f'- Refuter-style briefs matched to results: {len(pairs)}; with a refuted, weakened or wrong signal: {signal}. Verdict words: {dict(vc)}.']
    open(out, 'w').write('\n'.join(lines) + '\n')
    return lines


def refuter_pairs(corpus_dir):
    pairs = []
    for fn in sorted(glob.glob(os.path.join(corpus_dir, 'chunk-*.txt'))):
        for sess in open(fn).read().split('=== SESSION ')[1:]:
            sid, briefs = sess[:8], {}
            for e in re.split(r'\n(?=\[\d\d:\d\d [A-Z-]+\])', sess):
                m = re.match(r'\[\d\d:\d\d AGENT-BRIEF\] (.*?): ', e)
                if m:
                    briefs[m.group(1).strip()] = e; continue
                m = re.match(r'\[\d\d:\d\d TASK-DONE\] Agent "(.*?)" (finished|failed|stopped|was stopped)', e)
                if m and m.group(1).strip() in briefs:
                    pairs.append((sid, briefs[m.group(1).strip()], e))
                elif e.startswith('[') and 'AGENT-REPORT]' in e[:23] and briefs:
                    pairs.append((sid, list(briefs.values())[-1], e))
    return [p for p in pairs if REFUTER_BRIEF.search(p[1])]


# ---------------------------------------------------------------- github
class NotFound(Exception):
    pass


def gh_json(path):
    r = None
    for attempt in range(3):  # this link drops GitHub connections; retry before calling it an error
        try:
            r = subprocess.run(['gh', 'api', '--paginate', path], capture_output=True, text=True, timeout=120)
            if r.returncode == 0:
                break
        except subprocess.TimeoutExpired:
            r = None
        if r is not None and 'HTTP 404' in r.stderr:
            raise NotFound(path)
        time.sleep(5 * (attempt + 1))
    if r is None or r.returncode != 0:
        raise RuntimeError(f'gh api {path}: ' + ((r.stderr.strip()[:200]) if r else 'timed out'))
    out = r.stdout.strip()
    try:
        return json.loads(out)
    except Exception:  # --paginate concatenates JSON arrays as ][
        return json.loads('[' + out.replace('][', ',')[1:-1] + ']') if out.startswith('[') else json.loads(out)


CARRIED = os.path.join(MINE_ROOT, 'carried.json')


def github(meta, since, until, out):
    """Fetch each reviewed PR, then write github.md and the judges' batches. Returns (login, the number of PRs
    for the judges, errors); with errors, no batches are written and the agents wait for a rerun."""
    os.makedirs(os.path.join(out, 'gh'), exist_ok=True)
    try:
        r = subprocess.run(['gh', 'api', 'user', '--jq', '.login'], capture_output=True, text=True, timeout=60)
    except (subprocess.TimeoutExpired, OSError) as e:
        raise SystemExit(f'github stage: `gh api user` failed ({type(e).__name__})')
    login = r.stdout.strip()
    if r.returncode != 0 or not login:
        raise SystemExit('github stage: `gh api user` gave no login, so the owner\'s comments could not be told from others\'')
    first = {}
    for m in meta:
        if m['key']:
            k = tuple(m['key'])
            if k not in first or T(m['launch_ts']) < T(first[k]):
                first[k] = m['launch_ts']
    # PRs an earlier run reviewed and saw open: their maintainers' comments made in this window are judged here
    carried = {tuple(k.split('/')): v for k, v in (json.load(open(CARRIED)) if os.path.exists(CARRIED) else {}).items()}
    for k, v in carried.items():
        if k not in first or T(v) < T(first[k]):
            first[k] = v
    errors, unreachable = [], []
    for key in first:
        p = os.path.join(out, 'gh', '__'.join(key) + '.json')
        if os.path.exists(p):
            continue
        o, rp, n = key
        try:
            res = {name: gh_json(path) for name, path in (
                ('pr', f'repos/{o}/{rp}/pulls/{n}'), ('reviews', f'repos/{o}/{rp}/pulls/{n}/reviews?per_page=100'),
                ('comments', f'repos/{o}/{rp}/pulls/{n}/comments?per_page=100'),
                ('issue_comments', f'repos/{o}/{rp}/issues/{n}/comments?per_page=100'))}
            json.dump(res, open(p, 'w'))
        except NotFound:
            unreachable.append('/'.join(key))
        except Exception as e:
            errors.append(f'{"/".join(key)}: {e}')
    if errors:
        open(os.path.join(out, 'github.md'), 'w').write('# GitHub outcomes\n\nIncomplete; rerun to fetch:\n' + ''.join(f'- {e}\n' for e in errors))
        return login, 0, errors
    bot = lambda u: (u or {}).get('type') == 'Bot' or (u or {}).get('login', '').endswith('[bot]')
    after = lambda a, b: bool(a) and T(a) > T(b)
    inside = lambda a, lo: after(a, lo) and not after(a, iso(until))  # each comment is judged in one window only
    still_open = {}
    dist, longs, react, closed, batches = collections.Counter(), [], collections.Counter(), [], []
    for key, ts0 in first.items():
        gp = os.path.join(out, 'gh', '__'.join(key) + '.json')
        if not os.path.exists(gp):
            continue
        d = json.load(open(gp))
        lo = max(ts0, iso(since), key=T)  # judged here: after the first review, and inside this window
        cm = d['comments']; kids = collections.defaultdict(list)
        for c in cm:
            if c.get('in_reply_to_id'):
                kids[c['in_reply_to_id']].append(c)
        ours = [c for c in cm + d['issue_comments'] if c['user']['login'] == login and inside(c['created_at'], iso(since))]
        for c in ours:
            for k in ('+1', '-1', 'laugh', 'confused', 'heart', 'hooray', 'rocket', 'eyes'):
                react[k] += (c.get('reactions') or {}).get(k, 0)
        for c in cm:
            if c.get('in_reply_to_id') or c['user']['login'] != login or not inside(c['created_at'], iso(since)):
                continue
            th = [c] + sorted(kids.get(c['id'], []), key=lambda x: x['created_at'])
            dist[min(len(th), 6)] += 1
            if len(th) >= 4:
                longs.append('/'.join(key) + f" {c['path']}: " + ' | '.join(f"{x['user']['login']}: {x['body'][:140]}" for x in th[:3]).replace('\n', ' '))
        pr = d['pr']
        if pr.get('state') == 'open' or (pr.get('closed_at') and T(pr['closed_at']) > until):  # open at the window's end
            still_open['/'.join(key)] = ts0
        if pr.get('state') == 'closed' and not pr.get('merged_at') and ours:
            closed.append('/'.join(key) + ' — ' + ' | '.join(f"{x['user']['login']}: {x['body'][:160]}" for x in d['issue_comments'][-2:]).replace('\n', ' '))
        theirs = []
        for c in cm:
            if c.get('in_reply_to_id') or bot(c['user']) or c['user']['login'] in (login, pr['user']['login']) or not inside(c['created_at'], lo):
                continue
            theirs.append({'who': c['user']['login'], 'when': c['created_at'], 'where': f"{c['path']}:{c.get('line') or c.get('original_line')}",
                           'body': c['body'][:1500], 'thread': [{'who': x['user']['login'], 'body': x['body'][:600]} for x in kids.get(c['id'], [])]})
        for rv in d['reviews']:
            if rv.get('state') == 'CHANGES_REQUESTED' and not bot(rv['user']) and rv['user']['login'] != login and inside(rv.get('submitted_at'), lo):
                theirs.append({'who': rv['user']['login'], 'when': rv['submitted_at'], 'where': 'review body', 'body': (rv.get('body') or '')[:1500], 'thread': []})
        if theirs:
            posted = [{'when': c['created_at'], 'where': c.get('path', 'conversation') + (f":{c.get('line')}" if c.get('line') else ''),
                       'reply': bool(c.get('in_reply_to_id')), 'body': c['body'][:1200]} for c in cm + d['issue_comments'] if c['user']['login'] == login]
            posted += [{'when': rv.get('submitted_at'), 'where': 'review body', 'reply': False, 'body': rv['body'][:1200]}
                       for rv in d['reviews'] if rv['user']['login'] == login and (rv.get('body') or '').strip()]
            batches.append({'pr': f'https://github.com/{"/".join(key[:2])}/pull/{key[2]}', 'title': pr.get('title'), 'author': pr['user']['login'],
                            'our_first_review_session': ts0, 'merged': bool(pr.get('merged_at')), 'state': pr.get('state'),
                            'our_posted_comments': sorted(posted, key=lambda x: x['when'] or ''), 'their_comments_after_our_first_review': theirs})
    json.dump(still_open, open(os.path.join(out, 'carried-next.json'), 'w'), indent=1)
    os.makedirs(os.path.join(out, 'outcomes'), exist_ok=True)
    for k in range(0, len(batches), 18):
        json.dump(batches[k:k + 18], open(os.path.join(out, 'outcomes', f'batch-{k // 18 + 1:02d}.json'), 'w'), indent=1)
    tot = sum(dist.values())
    lines = ['# GitHub outcomes', '', f'- Account: {login}. PRs: {len(first)}.',
             f"- Our top-level inline comments in the window: {tot}; thread length (messages): {dict(sorted(dist.items()))}.",
             f'- Reactions on our comments in the window: {dict(react)}.',
             f"- PRs with other reviewers' later comments, for the judges: {len(batches)}. The judges see top-level inline comments and "
             "changes-requested reviews only; a point raised in a commented review's body or the conversation is not judged.",
             '', '## Threads of four or more messages'] + ['- ' + x for x in longs] + ['', '## Closed unmerged after our comments'] + ['- ' + x for x in closed] + \
            ['', '## Unreachable (404), skipped'] + ['- ' + x for x in unreachable]
    open(os.path.join(out, 'github.md'), 'w').write('\n'.join(lines) + '\n')
    return login, len(batches), []


# ---------------------------------------------------------------- memories
def memories(since, until, out_dir, chunk_chars=350000):
    """Review-related memories changed inside the window, in chunks one reader can read in full."""
    files = []
    for p in sorted(glob.glob(os.path.join(PROJECTS, '*', 'memory', '*.md'))):
        b = os.path.basename(p)
        mt = dt.datetime.fromtimestamp(os.path.getmtime(p), dt.timezone.utc)
        if b == 'MEMORY.md' or not (since < mt <= until):
            continue
        t = open(p, errors='replace').read()
        desc = (re.search(r'description:\s*"?(.*?)"?\n', t[:1200]) or [None, ''])[1]
        if REVIEWISH.search(b + ' ' + desc):
            files.append(f'\n##### FILE {os.path.basename(os.path.dirname(os.path.dirname(p)))}/{b}\n' + t)
    chunks, cur = [], []
    for f in files:
        if cur and sum(map(len, cur)) + len(f) > chunk_chars:
            chunks.append(cur); cur = []
        cur.append(f)
    if cur:
        chunks.append(cur)
    os.makedirs(out_dir, exist_ok=True)
    for k, c in enumerate(chunks, 1):
        open(os.path.join(out_dir, f'memories-{k:02d}.txt'), 'w').write(''.join(c))
    return len(files), len(chunks)


# ---------------------------------------------------------------- headless agents
def final_text(jsonl):
    """The agent's report. A Stop hook can draw replies after it: a short one amends the report and is
    appended; one at least half its length is a rewrite and replaces it."""
    texts_, res = [], {}
    for line in open(jsonl):
        try:
            d = json.loads(line)
        except Exception:
            continue
        if d.get('type') == 'assistant' and not d.get('parent_tool_use_id'):
            texts_ += [c['text'] for c in (d['message'].get('content') or []) if c.get('type') == 'text' and c['text'].strip()]
        if d.get('type') == 'result':
            res = d
    if not texts_:
        return res.get('result', '') or '', res
    longest, last = max(texts_, key=len), texts_[-1]
    if last is longest or len(last) >= len(longest) / 2:
        return last, res
    return longest + '\n\n## Amendment (after the Stop hook)\n' + last, res


def run_agent(d, brief, model, allow=None):
    """Run one headless agent in folder d with read-only tools. Skips a finished run. Its brief and logs go to
    d + '.log', outside its folder: agents given them beside their inputs spent calls reading their own stream."""
    rep = os.path.join(d, 'report.md')
    if os.path.exists(rep) and os.path.getsize(rep) > 200:
        return d, 'cached', 0.0
    log = d + '.log'
    os.makedirs(log, exist_ok=True)
    open(os.path.join(log, 'BRIEF.md'), 'w').write(brief)
    cmd = ['claude', '-p', brief, '--output-format', 'stream-json', '--verbose', '--strict-mcp-config',
           '--allowedTools', *(allow or READ_ONLY), '--disallowedTools', *DENY] + (['--model', model] if model else [])
    try:
        with open(os.path.join(log, 'out.jsonl'), 'w') as o, open(os.path.join(log, 'err.txt'), 'w') as e:
            rc = subprocess.run(cmd, cwd=d, stdin=subprocess.DEVNULL, stdout=o, stderr=e, timeout=4 * 3600).returncode
    except subprocess.TimeoutExpired:
        return d, 'timeout', 0.0
    body, res = final_text(os.path.join(log, 'out.jsonl'))
    cost = res.get('total_cost_usd') or 0.0
    status = res.get('subtype') or f'exit {rc}'
    if res.get('is_error'):
        status = 'error: ' + str(res.get('result') or '')[:80]
    if rc == 0 and status == 'success' and len(body) > 200:
        open(rep, 'w').write(body + f"\n\n<!-- cost=${cost:.2f} session {res.get('session_id')} -->\n")
    elif status == 'success':
        status = 'no report'
    return d, status, cost


def stage(d, files):
    os.makedirs(d, exist_ok=True)
    for name, src in files.items():
        dst = os.path.join(d, name)
        if isinstance(src, str) and src and os.path.exists(src):
            shutil.copy(src, dst)
        elif isinstance(src, str):
            open(dst, 'w').write(src)
    return d


def fill(name, **kv):
    t = open(os.path.join(BRIEFS, name)).read()
    for k, v in kv.items():
        t = t.replace('@@' + k + '@@', str(v))
    left = re.findall(r'@@\w+@@', t)
    if left:
        raise SystemExit(f'brief {name}: unfilled {left}')
    return t


def split_catalog(t):
    parts = re.split(r'(?m)^(#{1,2} \d+\.[^\n]*)\n', t)
    if len(parts) < 3:
        return t, t
    head, secs = parts[0], [(parts[i], parts[i + 1]) for i in range(1, len(parts), 2)]
    num = lambda h: int(re.match(r'#{1,2} (\d+)', h).group(1))
    return (head + ''.join(h + '\n' + b for h, b in secs if num(h) in (1, 2, 5)),
            head[:1500] + ''.join(h + '\n' + b for h, b in secs if num(h) in (3, 4)))


def ledger_extracts(dst):
    """Parked and killed pr-review items from the ledger, each with context from its own section only."""
    path = os.path.join(LESSONS, 'REJECTED.md')
    L = open(path).read().split('\n') if os.path.exists(path) else []
    part = [0] * len(L)  # each line's section
    for i in range(1, len(L)):
        part[i] = part[i - 1] + L[i].startswith('## ')
    named = {part[i] for i, l in enumerate(L) if 'pr-review' in l}  # sections whose heading or body names pr-review
    span = lambda a, b, i: ' '.join(L[k].strip() for k in range(max(0, a), min(b, len(L))) if part[k] == part[i])
    parked, killed, sec, under = [], [], '', False
    for i, l in enumerate(L):
        if l.startswith('## '):
            sec = l[3:]
        rel = 'pr-review' in sec or ('owner-directed' in sec and part[i] in named) or 'pr-review' in span(i - 6, i + 1, i)
        if rel and 'REOPEN ON' in l:
            j = i
            while j > 0 and not re.match(r'\s*(- )?\*\*', L[j]) and i - j < 12:
                j -= 1
            parked.append(f'[{i + 1}] ({sec[:60]}) ' + span(j, i + 2, i)[:420])
        if l.startswith('**') or l.startswith('## '):
            under = bool(re.match(r'(?i)\*\*(rejected|declined|killed|checked, no change|left out)', l))
        if rel and (re.search(r'(?i)\*\*killed|killed by gate|\b(killed|declined|rejected)\b', l) or (under and l.lstrip().startswith('- '))):
            killed.append(f'[{i + 1}] ({sec[:60]}) ' + span(i, i + 6, i)[:600])
    open(os.path.join(dst, 'parked.txt'), 'w').write('\n\n'.join(parked) + '\n')
    open(os.path.join(dst, 'killed.md'), 'w').write('\n\n'.join(killed) + '\n')


def agents(out, args, n_chunks, sessions_n, window, login, n_mem, since, until):
    common = {'SKILL.md': os.path.join(SKILLS, 'pr-review', 'SKILL.md'), 'posting.md': os.path.join(SKILLS, 'pr-review', 'posting.md'),
              'PRS-private.txt': PRS_FILE if os.path.exists(PRS_FILE) else ''}
    version = (re.search(r'^version:\s*(\S+)', open(common['SKILL.md']).read(), re.M) or [None, '?'])[1]
    costs, fails = [], []

    def go(jobs):
        with cf.ThreadPoolExecutor(args.parallel) as ex:
            for d, status, cost in ex.map(lambda j: run_agent(j[0], j[1], args.model, j[2] if len(j) > 2 else None), jobs):
                costs.append(cost)
                if status not in ('success', 'cached'):
                    fails.append(f'{os.path.relpath(d, out)}: {status}')

    # 1. readers, outcomes judges, memory readers, auditor
    jobs = []
    for k in range(1, n_chunks + 1):
        ch = os.path.join(out, 'corpus', f'chunk-{k:02d}.txt'); txt = open(ch).read()
        heads = re.findall(r'^=== SESSION .*?\| (\d{4}-\d\d-\d\dT[\d:]{5}) ->', txt, re.M)
        d = stage(os.path.join(out, 'readers', f'r{k:02d}'), {**common, 'chunk.txt': ch})
        jobs.append((d, fill('reader.md', N_READERS=n_chunks, SESSIONS_N=len(heads), VERSION=version,
                                 DATE_RANGE=f'{heads[0][:10]} to {heads[-1][:10]}' if heads else window)))
    for b in sorted(glob.glob(os.path.join(out, 'outcomes', 'batch-*.json'))):
        d = stage(os.path.join(out, 'judges', os.path.basename(b)[:-5]), {'batch.json': b, 'PRS-private.txt': common['PRS-private.txt']})
        jobs.append((d, fill('outcomes.md', LOGIN=login)))
    for m in sorted(glob.glob(os.path.join(out, 'memories', 'memories-*.txt'))):
        d = stage(os.path.join(out, 'aux', os.path.basename(m)[:-4]), {**common, 'memories.txt': m})
        jobs.append((d, fill('memories.md', VERSION=version, N_FILES=open(m).read().count('\n##### FILE '))))
    d = stage(os.path.join(out, 'aux', 'audit'), {**common, 'skill-retro.SKILL.md': os.path.join(SKILLS, 'skill-retro', 'SKILL.md')})
    words = lambda p: len(open(p).read().split())
    jobs.append((d, fill('audit.md', VERSION=version, SKILL_WORDS=words(common['SKILL.md']), POSTING_WORDS=words(common['posting.md']))))
    go(jobs)
    if fails:  # a merge over missing inputs would be cached and never revisited
        return version, costs, fails
    reports = sorted(glob.glob(os.path.join(out, 'readers', 'r*', 'report.md')))
    # 2. synthesizers, ten reader reports each
    jobs = []
    for k in range(0, len(reports), 10):
        files = {**common, **{f'report-{os.path.basename(os.path.dirname(r))[1:]}.md': r for r in reports[k:k + 10]}}
        jobs.append((stage(os.path.join(out, 'synth', f's{k // 10 + 1}'), files), fill('synth.md', VERSION=version)))
    go(jobs)
    if fails:
        return version, costs, fails
    cats = sorted(glob.glob(os.path.join(out, 'synth', 's*', 'report.md')))
    # 3. final mergers
    try:
        fr = subprocess.run(['git', '-C', args.repo, 'fetch', '-q', 'origin'], capture_output=True, text=True, timeout=300)
    except subprocess.TimeoutExpired:
        fr = None
    if fr is None or fr.returncode != 0:
        fails.append(f'git fetch in {args.repo} failed, so the skill history would be stale: ' + ((fr.stderr.strip()[:120]) if fr else 'timed out'))
        return version, costs, fails
    hist = subprocess.run(['git', '-C', args.repo, 'log', 'origin/main', '--format=%ad %h %s', '--date=short', '--',
                           '.claude/skills/pr-review/'], capture_output=True, text=True).stdout
    full = subprocess.run(['git', '-C', args.repo, 'log', '-p', '--reverse', 'origin/main', '--format=commit %ad %h %s', '--date=short', '--',
                           '.claude/skills/pr-review/'], capture_output=True, text=True).stdout
    if not hist.strip():
        fails.append(f'no pr-review history from {args.repo}: pass --repo <querystore checkout>')
        return version, costs, fails
    fin = os.path.join(out, 'final'); D1, D2 = os.path.join(fin, 'skill-text'), os.path.join(fin, 'machinery')
    for x in (D1, D2):
        stage(x, {**common, 'measurements.md': os.path.join(out, 'measurements.md')})
        os.makedirs(os.path.join(x, 'runrecords'), exist_ok=True)
        for r in glob.glob(os.path.join(LESSONS, '*pr-review*.md')):
            if since < dt.datetime.fromtimestamp(os.path.getmtime(r), dt.timezone.utc) <= until:
                shutil.copy(r, os.path.join(x, 'runrecords'))
    for i, c in enumerate(cats):
        a, b = split_catalog(open(c).read())
        open(os.path.join(D1, f'catalog-{i + 1}.md'), 'w').write(a)
        open(os.path.join(D2, f'catalog-{i + 1}-timesinks-practices.md'), 'w').write(b)
    for src, name in ((os.path.join(out, 'aux', 'audit', 'report.md'), 'auditor.md'), (os.path.join(out, 'github.md'), 'github.md')):
        if os.path.exists(src):
            shutil.copy(src, os.path.join(D1, name))
    for j in sorted(glob.glob(os.path.join(out, 'judges', '*', 'report.md'))):
        shutil.copy(j, os.path.join(D1, 'outcomes-' + os.path.basename(os.path.dirname(j)) + '.md'))
    for r in sorted(glob.glob(os.path.join(out, 'aux', 'memories-*', 'report.md'))):
        shutil.copy(r, os.path.join(D2, 'memreader-' + os.path.basename(os.path.dirname(r))[-2:] + '.md'))
    recipes = os.path.join(HOME, '.claude', 'pr-review-recipes.md')
    stage(D2, {'recipes.md': recipes if os.path.exists(recipes) else '(no recipes file yet)\n'})
    ledger_extracts(D1); ledger_extracts(D2)
    open(os.path.join(D1, 'pr-review-history.txt'), 'w').write(hist)
    open(os.path.join(D1, 'pr-review-history-full.txt'), 'w').write(full)
    go([(D1, fill('skill-text.md', VERSION=version, SESSIONS=sessions_n, WINDOW=window)),
        (D2, fill('machinery.md', VERSION=version, SESSIONS=sessions_n, WINDOW=window, N_MEM=n_mem))])
    return version, costs, fails


# ---------------------------------------------------------------- the run's window
def finished(run):
    return all(os.path.exists(os.path.join(run, 'final', x, 'report.md')) for x in ('skill-text', 'machinery'))


def resolve_window(args, started):
    """The run folder and its fixed window. A bare command resumes the newest unfinished run."""
    explicit = bool(args.since or args.until)
    out = args.out
    if not out and not explicit:
        runs = sorted(r for r in glob.glob(os.path.join(MINE_ROOT, '20*')) if os.path.exists(os.path.join(r, 'window.json')))
        auto = lambda r: (lambda w: w['from_watermark'] and not w.get('dry_run'))(json.load(open(os.path.join(r, 'window.json'))))
        open_runs = [r for r in runs if not finished(r) and auto(r)]
        out = open_runs[-1] if open_runs else None
    if not out:
        out = os.path.join(MINE_ROOT, started.strftime('%Y-%m-%d'))
        k = 2
        while os.path.exists(os.path.join(out, 'window.json')):
            out = os.path.join(MINE_ROOT, started.strftime('%Y-%m-%d') + f'-{k}'); k += 1
    wj = os.path.join(out, 'window.json')
    if os.path.exists(wj):
        w = json.load(open(wj))
        if explicit and (args.since and whole(args.since) != T(w['since']) or args.until and whole(args.until) != T(w['until'])):
            raise SystemExit(f'{out} holds another window ({w["since"]} to {w["until"]}); pass --out for a new folder')
        return out, T(w['since']), T(w['until']), w['from_watermark']
    if args.since:
        since = whole(args.since)
    elif os.path.exists(WATERMARK):
        since = T(open(WATERMARK).read().strip())
    else:
        raise SystemExit('no watermark: pass --since <ISO time> for the first run')
    until = whole(args.until) if args.until else started.replace(microsecond=0)  # as window.json and the watermark store it
    os.makedirs(out, exist_ok=True)
    json.dump({'since': iso(since), 'until': iso(until), 'from_watermark': not explicit, 'dry_run': bool(getattr(args, 'dry_run', False))}, open(wj, 'w'))
    return out, since, until, not explicit


# ---------------------------------------------------------------- main
def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument('--since', help='ISO time, UTC unless it names a zone; default: the watermark in ~/.claude/pr-review-mining/MINED')
    ap.add_argument('--until', help='ISO time, UTC unless it names a zone; default: when the run started')
    ap.add_argument('--out', help='run folder; default: the newest unfinished run, else ~/.claude/pr-review-mining/<date>')
    ap.add_argument('--parallel', type=int, default=5, help='headless agents at once (default 5)')
    ap.add_argument('--chunk-chars', type=int, default=430000, help='corpus chunk size; one reader each')
    ap.add_argument('--model', help='model for the headless agents; default: the CLI default')
    ap.add_argument('--repo', default=os.path.join(HOME, 'Projects', 'openmrs', 'openmrs-module-querystore'),
                    help='querystore checkout, for dating skill rules')
    ap.add_argument('--no-github', action='store_true', help='skip the GitHub outcomes stage')
    ap.add_argument('--dry-run', action='store_true', help='scan, corpus, measure, github and memories only; no agents, watermark kept')
    ap.add_argument('--self-test', action='store_true', help='check the measurements against synthetic transcripts, then exit')
    args = ap.parse_args()
    if args.self_test:
        return self_test()
    started = dt.datetime.now(dt.timezone.utc)
    out, since, until, from_watermark = resolve_window(args, started)
    window = f'{iso(since)} to {iso(until)}'
    wj = os.path.join(out, 'window.json')
    again = f'rerun with --out {shlex.quote(out)}' + (' --dry-run' if args.dry_run else '')  # resumes any run, re-mines and dry runs included
    print(f'window {window}; output {out}', flush=True)

    def hinted(stage, *a):
        """A stage that stops the run says how to resume it: a re-mine without --out would start a new folder."""
        try:
            return stage(*a)
        except SystemExit as e:
            raise SystemExit(f'{e}\nfix that, then {again}') from None
    sp = os.path.join(out, 'scan.json')
    sessions = json.load(open(sp)) if os.path.exists(sp) else scan(since, until)
    json.dump(sessions, open(sp, 'w'))
    if not sessions:  # remove only what this run wrote: a --out folder may hold other files
        for f in (wj, sp):
            os.remove(f)
        with contextlib.suppress(OSError):
            os.rmdir(out)
        print('no pr-review sessions in the window; nothing mined, watermark unchanged'); return
    cdir = os.path.join(out, 'corpus')
    if os.path.exists(os.path.join(cdir, 'stats.json')):
        st = json.load(open(os.path.join(cdir, 'stats.json'))); n_chunks, n_blocks = st['chunks'], st['sessions']
    else:
        n_chunks, n_blocks, st = hinted(build_corpus, sessions, since, until, load_private(), cdir, args.chunk_chars)
    print(f"sessions {n_blocks}; chunks {n_chunks}; background launches {st.get('async_launches', 0)}, background reports {st.get('bg_results', 0)}", flush=True)
    mp = os.path.join(out, 'measurements.md')
    if not os.path.exists(mp):
        measure(sessions, since, until, cdir, mp)
    print(open(mp).read(), flush=True)
    meta = json.load(open(os.path.join(cdir, 'sessions.json')))
    login, n_batches, gh_errors = ('', 0, []) if args.no_github else hinted(github, meta, since, until, out)
    mdir = os.path.join(out, 'memories')
    if os.path.isdir(mdir):
        n_mem = sum(open(m).read().count('\n##### FILE ') for m in glob.glob(os.path.join(mdir, 'memories-*.txt')))
    else:
        n_mem, _ = memories(since, until, mdir)
    print(f'github: PRs for the judges {n_batches}, fetch errors {len(gh_errors)}; memories changed {n_mem}', flush=True)
    if gh_errors:
        print(f'github fetch incomplete; {again} to fetch the rest before any agent runs:')
        print('\n'.join('  ' + e for e in gh_errors)); return
    if args.dry_run:
        print('dry run: no agents; watermark unchanged'); return
    version, costs, fails = hinted(agents, out, args, n_chunks, n_blocks, window, login, n_mem, since, until)
    summary = [f'# Review mining {window}', '', f'- pr-review {version}; sessions {n_blocks}; chunks {n_chunks}; agents run now {len(costs)}; cost of this run ${sum(costs):.2f}.',
               '- Decision lists: `final/skill-text/report.md` (skill text) and `final/machinery/report.md` (scripts, hooks, recipes, memories).',
               '- Evidence: `measurements.md`, `github.md`, `readers/`, `synth/`, `judges/`, `aux/`; each agent\'s brief and stream in `<its folder>.log/`.'] + \
              (['', f'## Failures ({again} to resume)'] + ['- ' + f for f in fails] if fails else [])
    open(os.path.join(out, 'SUMMARY.md'), 'w').write('\n'.join(summary) + '\n')
    print('\n'.join(summary))
    if not from_watermark:
        print('watermark unchanged: --since or --until was given')
    elif args.no_github:
        print('watermark unchanged: --no-github skipped this window\'s outcomes; rerun without it to judge them')
    elif not finished(out) or fails:
        print(f'watermark unchanged: the run is incomplete; {again} to resume')
    elif not os.path.exists(WATERMARK) or T(open(WATERMARK).read().strip()) != since:
        print(f'watermark unchanged: another run moved it after this run began at {iso(since)}')
    else:
        open(WATERMARK, 'w').write(iso(until) + '\n')
        if os.path.exists(os.path.join(out, 'carried-next.json')):
            shutil.copy(os.path.join(out, 'carried-next.json'), CARRIED)
        print(f'watermark moved to {iso(until)}')


# ---------------------------------------------------------------- self-test
def self_test():
    """Synthetic transcripts with known answers; each check names the trap it guards."""
    global PROJECTS, PRS_FILE, MINE_ROOT, WATERMARK, CARRIED, SKILLS, LESSONS, HOME, run_agent
    tmp = tempfile.mkdtemp(); ok = True
    PROJECTS, PRS_FILE = os.path.join(tmp, 'projects'), os.path.join(tmp, 'PRS')
    MINE_ROOT = os.path.join(tmp, 'mining'); WATERMARK = os.path.join(MINE_ROOT, 'MINED'); CARRIED = os.path.join(MINE_ROOT, 'carried.json')
    fold = os.path.join(PROJECTS, '-Users-x-Projects-openmrs-openmrs-core'); os.makedirs(fold)
    other = os.path.join(PROJECTS, '-Users-x-Projects-openmrs-fhir2'); os.makedirs(other)
    t0 = dt.datetime(2026, 1, 1, 10, 0, tzinfo=dt.timezone.utc)
    at = lambda s: iso(t0 + dt.timedelta(seconds=s))
    L = [{'type': 'user', 'timestamp': at(0), 'message': {'content': 'Base directory for this skill: /h/.claude/skills/pr-review\n\nArguments: `https://github.com/openmrs/openmrs-core/pull/9999 --post`'}},
         {'type': 'assistant', 'timestamp': at(60), 'message': {'id': 'm1', 'usage': {'output_tokens': 100000}, 'content': [{'type': 'tool_use', 'id': 't1', 'name': 'Bash', 'input': {'command': 'mvn -q test'}}]}},
         {'type': 'assistant', 'timestamp': at(60), 'message': {'id': 'm1', 'usage': {'output_tokens': 100000}, 'content': [{'type': 'tool_use', 'id': 'a1', 'name': 'Agent', 'input': {'description': 'Refute finding', 'prompt': 'Try to refute this finding.', 'run_in_background': True}}]}},
         {'type': 'user', 'timestamp': at(180), 'message': {'content': [{'type': 'tool_result', 'tool_use_id': 't1', 'content': 'x' * 50}]}},
         {'type': 'user', 'timestamp': at(181), 'message': {'content': [{'type': 'tool_result', 'tool_use_id': 'a1', 'content': ASYNC_ACK + '. agentId: z'}]}},
         {'type': 'assistant', 'timestamp': at(240), 'message': {'id': 'm2', 'usage': {'output_tokens': 50}, 'content': [{'type': 'text', 'text': 'Waiting on the refuter.'}]}},
         {'type': 'user', 'timestamp': at(840), 'message': {'content': '<task-notification>\n<summary>Agent "Refute finding" finished</summary>\n<status>completed</status>\n<result>REFUTED: the claim is wrong on base.</result>\n</task-notification>'}},
         {'type': 'user', 'timestamp': at(850), 'isMeta': True, 'message': {'content': 'Your claude.ai usage limit has reset. Continue the task.'}},
         {'type': 'assistant', 'timestamp': at(900), 'message': {'id': 'm3', 'usage': {'output_tokens': 70}, 'content': [{'type': 'text', 'text': 'Report: one finding, refuted.'}]}},
         {'type': 'user', 'timestamp': at(4000), 'message': {'content': 'is it ready to merge?'}},
         {'type': 'assistant', 'timestamp': at(9000), 'message': {'id': 'm4', 'usage': {'output_tokens': 10}, 'content': [{'type': 'text', 'text': 'LATE ENTRY after the window.'}]}}]
    path = os.path.join(fold, 'aaaaaaaa-0000.jsonl')
    open(path, 'w').write('\n'.join(json.dumps(x) for x in L) + '\n')
    # a session launched by bare number from another folder, for a number only the private list holds
    open(os.path.join(other, 'bbbbbbbb-0000.jsonl'), 'w').write(json.dumps(
        {'type': 'user', 'timestamp': at(100), 'message': {'content': 'Base directory for this skill: /h/.claude/skills/pr-review\n\nArguments: `4242`'}}) + '\n')
    # a private number written as repo#n, and a public #n control
    for sid, a in (('cccccccc', 'openmrs-core#4242'), ('dddddddd', '#1234 https://github.com/openmrs/openmrs-core/pull/5555')):
        open(os.path.join(other, sid + '-0000.jsonl'), 'w').write(json.dumps(
            {'type': 'user', 'timestamp': at(120), 'message': {'content': 'Base directory for this skill: /h/.claude/skills/pr-review\n\nArguments: `' + a + '`'}}) + '\n')
    # a PRS line with a URL and a note of several words, and a line with only a URL
    open(PRS_FILE, 'w').write('https://github.com/openmrs/openmrs-core/pull/9999 SEC-1 a note of several words\nhttps://github.com/openmrs/openmrs-core/pull/4242\n')

    def check(name, got, want):
        nonlocal ok
        good = got == want
        ok &= good
        print(f"{'ok  ' if good else 'FAIL'} {name}: got {got!r}, want {want!r}")

    since, until = t0 - dt.timedelta(hours=1), t0 + dt.timedelta(seconds=5000)
    ss = scan(since, until)
    check('scan finds every launch', len(ss), 4)
    main_s = [s for s in ss if s['sid'] == 'aaaaaaaa'][0]
    seg = timeline(main_s, since, until)
    # trap: a background agent's wait is idle time between turns, not a long tool call
    check('model seconds', round(seg['model']), 60 + 59 + 50)
    check('foreground tool seconds', round(seg['foreground tools']), 121)
    check('background wait seconds (not counted as model time)', round(seg['background agents and tasks']), 600)
    # trap: a meta notice ("usage limit has reset") is not the owner's reply
    check('a meta notice does not end the timeline', round(seg['other (mostly background waits ended by compaction)']), 10)
    check('timeline stops at the first typed reply (the 3,100 s the owner took is not counted)', round(sum(seg.values())), 900)
    out = os.path.join(tmp, 'corpus')
    n, b, tot = build_corpus(ss, since, until, load_private(), out, 100000)
    txt = open(os.path.join(out, 'chunk-01.txt')).read()
    # trap: background agents' reports arrive as task notifications, not tool results
    check('corpus carries the background report', 'REFUTED: the claim is wrong' in txt, True)
    check('corpus drops the launch acknowledgement', ASYNC_ACK in txt, False)
    check('corpus flags the private PR named with a note in PRS', 'PRIVATE PR' in [l for l in txt.split('\n') if 'aaaaaaaa' in l][0], True)
    check('corpus flags a bare private number launched from another folder', 'PRIVATE PR' in [l for l in txt.split('\n') if 'bbbbbbbb' in l][0], True)
    check('corpus flags a private number written as repo#n', 'PRIVATE PR' in [l for l in txt.split('\n') if 'cccccccc' in l][0], True)
    check('corpus leaves a public #n public', 'PRIVATE PR' in [l for l in txt.split('\n') if 'dddddddd' in l][0], False)
    check('corpus keeps the typed question', 'is it ready to merge?' in txt, True)
    # trap: entries after the window end belong to the next run
    check('corpus stops at the window end', 'LATE ENTRY' in txt, False)
    check('corpus control passes a healthy corpus', corpus_control({'async_launches': 10, 'bg_results': 8})[0], True)
    check('corpus control fails a corpus missing background reports', corpus_control({'async_launches': 401, 'bg_results': 1})[0], False)
    pr = refuter_pairs(out)
    check('refuter brief matched to its result', len(pr), 1)
    check('verdict signal read', bool(re.search(VERDICTS[0][1], pr[0][2])), True)
    measure([main_s], since, until, out, os.path.join(tmp, 'm.md'))
    m = open(os.path.join(tmp, 'm.md')).read()
    # trap: one response spans several lines that repeat its usage; count each message id once
    check('output tokens deduplicated by message id (100k once, not 200k)', 'median 100k' in m, True)
    check('refuter count in measurements', 'matched to results: 1; with a refuted, weakened or wrong signal: 1' in m, True)
    # trap: a Stop hook draws replies after the real report
    def report_of(replies):
        j = os.path.join(tmp, 'r.jsonl')
        open(j, 'w').write('\n'.join(json.dumps({'type': 'assistant', 'message': {'content': [{'type': 'text', 'text': r}]}}) for r in replies) + '\n')
        return final_text(j)[0]
    body = report_of(['R' * 3000, 'reworded lesson 16'])
    check('a short amendment is appended to the report', (body.startswith('R' * 3000), 'reworded lesson 16' in body), (True, True))
    check('a rewrite at least half as long replaces the report', report_of(['R' * 3000, 'W' * 1600]) == 'W' * 1600, True)
    # trap: a run resumed later must keep its first window, and an explicit window must not move the watermark
    ns = argparse.Namespace(since=None, until=None, out=None)
    os.makedirs(MINE_ROOT); open(WATERMARK, 'w').write(iso(since) + '\n')
    o1, s1, u1, fw1 = resolve_window(ns, t0)
    o2, s2, u2, fw2 = resolve_window(ns, t0 + dt.timedelta(days=1))
    check('a bare rerun resumes the unfinished run and its window', (o2 == o1, u2 == u1, fw1, fw2), (True, True, True, True))
    o3, s3, u3, fw3 = resolve_window(argparse.Namespace(since=iso(since), until=None, out=os.path.join(tmp, 'adhoc')), t0)
    check('an explicit --since does not start from the watermark', fw3, False)
    # trap: a dry run must never be resumed as next month's real run
    shutil.rmtree(MINE_ROOT); os.makedirs(MINE_ROOT); open(WATERMARK, 'w').write(iso(since) + '\n')
    od, *_ = resolve_window(argparse.Namespace(since=None, until=None, out=None, dry_run=True), t0)
    oreal, *_ = resolve_window(argparse.Namespace(since=None, until=None, out=None, dry_run=False), t0 + dt.timedelta(days=30))
    check('a bare run after a dry run starts its own window', oreal != od, True)
    # trap: a window ending mid-second is mined to that instant but stored, and resumed, to the whole second
    o4, s4, u4, _ = resolve_window(argparse.Namespace(since=None, until=None, out=os.path.join(tmp, 'usec')), t0 + dt.timedelta(microseconds=500000))
    check('a window ends on the whole second window.json stores', (u4.microsecond, T(json.load(open(os.path.join(o4, 'window.json')))['until']) == u4), (0, True))
    fr = argparse.Namespace(since='2026-01-01T09:00:00.5Z', until='2026-01-01T12:00:00.5Z', out=os.path.join(tmp, 'frac'))
    first = resolve_window(fr, t0)[:3]
    try:
        rerun = resolve_window(fr, t0)[:3]
    except SystemExit as e:
        rerun = str(e)
    check('a re-mine given fractional times resumes its whole-second window', (rerun == first, first[1].microsecond, first[2].microsecond), (True, 0, 0))
    # trap: a date with no zone was read as local time, shifting the window, and then failed against aware mtimes
    nz = argparse.Namespace(since='2026-01-01T09:00:00', until='2026-01-02', out=os.path.join(tmp, 'naive'))
    try:
        w1 = resolve_window(nz, t0)[:3]; w2 = resolve_window(nz, t0)[:3]
        got = (json.load(open(os.path.join(tmp, 'naive', 'window.json')))['since'], w1 == w2, isinstance(scan(w1[1], w1[2]), list))
    except (Exception, SystemExit) as e:
        got = repr(e)
    check('a time with no zone is read as UTC, resumed, and scanned', got, ('2026-01-01T09:00:00Z', True, True))
    # the github stage, against a fake gh: a 404 is skipped, and only comments inside the window count
    fake = os.path.join(tmp, 'bin'); os.makedirs(fake)
    pr = {'state': 'open', 'merged_at': None, 'title': 't', 'user': {'login': 'author'}}
    cm = [{'id': 1, 'user': {'login': 'tester'}, 'created_at': at(100), 'path': 'A.java', 'line': 3, 'body': 'ours'},
          {'id': 2, 'user': {'login': 'maint'}, 'created_at': at(200), 'path': 'A.java', 'line': 9, 'body': 'inside'},
          {'id': 3, 'user': {'login': 'maint'}, 'created_at': at(9000), 'path': 'A.java', 'line': 9, 'body': 'after the window'},
          {'id': 4, 'user': {'login': 'maint'}, 'created_at': at(-100), 'path': 'A.java', 'line': 9, 'body': 'before our review'}]
    json.dump({'pulls/9999': pr, 'pulls/9999/reviews': [], 'pulls/9999/comments': cm, 'issues/9999/comments': []}, open(os.path.join(tmp, 'gh.json'), 'w'))
    open(os.path.join(fake, 'gh'), 'w').write('#!' + sys.executable + '\nimport sys,json\na=sys.argv[1:]\n'
        'import os\nif a[:2]==["api","user"]: print("" if os.environ.get("FAKE_GH_NO_LOGIN") else "tester"); sys.exit(0)\n'
        'p=a[-1].split("?")[0]; d=json.load(open(' + repr(os.path.join(tmp, 'gh.json')) + '))\n'
        'k=[x for x in d if p.endswith(x)]\n'
        'if not k: sys.stderr.write("gh: Not Found (HTTP 404)"); sys.exit(1)\n'
        'print(json.dumps(d[max(k,key=len)]))\n')
    os.chmod(os.path.join(fake, 'gh'), 0o755); old = os.environ['PATH']; os.environ['PATH'] = fake + os.pathsep + old
    try:
        gout = os.path.join(tmp, 'ghrun'); os.makedirs(gout)
        login, nb, errs = github(json.load(open(os.path.join(out, 'sessions.json'))), since, until, gout)
    finally:
        os.environ['PATH'] = old
    g = open(os.path.join(gout, 'github.md')).read()
    check('github: a 404 PR is listed as unreachable, not an error', ('openmrs/openmrs-core/5555' in g, 'openmrs/openmrs-core/9999' in g.split('Unreachable')[1], errs), (True, False, []))
    items = [i['body'] for b in json.load(open(os.path.join(gout, 'outcomes', 'batch-01.json'))) for i in b['their_comments_after_our_first_review']]
    check('github: only comments inside the window and after our review are judged', items, ['inside'])
    check('github: an open PR is carried to the next run', 'openmrs/openmrs-core/9999' in json.load(open(os.path.join(gout, 'carried-next.json'))), True)
    # trap: a review carried from an earlier run is earlier than this window's launch, and the earlier one wins
    meta = json.load(open(os.path.join(out, 'sessions.json')))
    for name, carried_ts, want in (('earlier', at(-500), at(-500)), ('later', at(3000), at(0))):
        json.dump({'openmrs/openmrs-core/9999': carried_ts}, open(CARRIED, 'w'))
        gdir = os.path.join(tmp, 'ghrun-' + name); os.makedirs(gdir)
        os.environ['PATH'] = fake + os.pathsep + old
        try:
            github(meta, since, until, gdir)
        finally:
            os.environ['PATH'] = old
        first_seen = json.load(open(os.path.join(gdir, 'outcomes', 'batch-01.json')))[0]['our_first_review_session']
        check(f'github: a carried review {name} than this window\'s launch gives the first review time', first_seen, want)
    os.remove(CARRIED)
    # trap: a PR open at the window's end is carried though it closed before the fetch; one closed inside the window is not
    gd = json.load(open(os.path.join(tmp, 'gh.json')))
    for n, closed in (('7777', at(9000)), ('7778', at(1000))):
        gd.update({f'pulls/{n}': {**pr, 'state': 'closed', 'closed_at': closed}, f'pulls/{n}/reviews': [], f'pulls/{n}/comments': [], f'issues/{n}/comments': []})
    json.dump(gd, open(os.path.join(tmp, 'gh.json'), 'w'))
    json.dump({'openmrs/openmrs-core/7777': at(0), 'openmrs/openmrs-core/7778': at(0)}, open(CARRIED, 'w'))
    gdir = os.path.join(tmp, 'ghrun-closed'); os.makedirs(gdir)
    os.environ['PATH'] = fake + os.pathsep + old
    try:
        github(meta, since, until, gdir)
    finally:
        os.environ['PATH'] = old
    nxt = json.load(open(os.path.join(gdir, 'carried-next.json')))
    check('github: a PR closed after the window\'s end is carried, one closed inside it is not', ('openmrs/openmrs-core/7777' in nxt, 'openmrs/openmrs-core/7778' in nxt), (True, False))
    os.remove(CARRIED)
    # trap: without gh the run must stop with a message, not a traceback
    os.environ['PATH'] = os.path.join(tmp, 'no-such-bin')
    try:
        github(meta, since, until, os.path.join(tmp, 'ghrun-nogh')); got = 'returned'
    except SystemExit as e:
        got = str(e)
    except Exception as e:
        got = repr(e)
    finally:
        os.environ['PATH'] = old
    check('github: no gh stops the run with a message', 'gh api user` failed' in got, True)
    # trap: a launch after the window's end belongs to the next run
    check('launch time: the first launch inside the window', launch_ts({'launches': [{'ts': at(-7200)}, {'ts': at(100)}, {'ts': at(9000)}]}, since, until), at(100))
    check('launch time: a launch after the window is not this one\'s', launch_ts({'launches': [{'ts': at(-7200)}, {'ts': at(9000)}]}, since, until), at(-7200))
    # trap: a memory changed after the window's end belongs to the next run
    mem = os.path.join(fold, 'memory'); os.makedirs(mem)
    for name, when in (('before', -7200), ('inside', 100), ('after', 9000)):
        p = os.path.join(mem, f'feedback_review_{name}.md')
        open(p, 'w').write(f'---\nname: r-{name}\ndescription: "a review lesson, {name}"\n---\nbody\n')
        os.utime(p, (t0.timestamp() + when,) * 2)
    check('memories: only those changed inside the window', memories(since, until, os.path.join(tmp, 'mem'))[0], 1)
    # trap: an agent given its own log beside its inputs reads it; and it must run with read-only tools only
    fakec = os.path.join(tmp, 'cbin'); os.makedirs(fakec)
    open(os.path.join(fakec, 'claude'), 'w').write('#!' + sys.executable + '\nimport json, os, sys\n'
        'json.dump({"argv": sys.argv[1:], "cwd": sorted(os.listdir("."))}, open(' + repr(os.path.join(tmp, 'claude-call.json')) + ', "w"))\n'
        'print(json.dumps({"type": "assistant", "message": {"content": [{"type": "text", "text": "R" * 300}]}}))\n'
        'print(json.dumps({"type": "result", "subtype": "success", "total_cost_usd": 0.01, "session_id": "s"}))\n')
    os.chmod(os.path.join(fakec, 'claude'), 0o755)
    ad = stage(os.path.join(tmp, 'agentrun', 'r01'), {'chunk.txt': 'text'})
    os.environ['PATH'] = fakec + os.pathsep + old
    try:
        status = run_agent(ad, 'the brief', None)[1]
    finally:
        os.environ['PATH'] = old
    call = json.load(open(os.path.join(tmp, 'claude-call.json'))); al = call['argv']
    check('an agent sees only its inputs, runs read-only, and its report is kept',
          (status, call['cwd'], al[al.index('--allowedTools') + 1:al.index('--disallowedTools')], '--dangerously-skip-permissions' in al,
           os.path.exists(os.path.join(ad, 'report.md')), sorted(os.listdir(ad + '.log')) if os.path.isdir(ad + '.log') else None),
          ('success', ['chunk.txt'], READ_ONLY, False, True, ['BRIEF.md', 'err.txt', 'out.jsonl']))
    # the driver end to end, each agent stubbed: what reaches the mergers, and when the watermark moves
    SKILLS, LESSONS, HOME = os.path.join(tmp, 'skills'), os.path.join(tmp, 'lessons'), tmp
    for f, body in (('pr-review/SKILL.md', '---\nversion: 9.9.9\n---\nReview.\n'), ('pr-review/posting.md', 'Post.\n'), ('skill-retro/SKILL.md', 'Retro.\n')):
        os.makedirs(os.path.dirname(os.path.join(SKILLS, f)), exist_ok=True); open(os.path.join(SKILLS, f), 'w').write(body)
    os.makedirs(LESSONS)
    open(os.path.join(LESSONS, 'REJECTED.md'), 'w').write('## 2026-01-01 (owner-directed: pr-review)\n\n**Rejected or parked:**\n'
                                                           + ''.join(f'- Item {w}: parked.\n' for w in ('one', 'two', 'three', 'four', 'five', 'six'))
                                                           + '\n## 2026-01-02 (owner-directed: harden\'s gate sentences)\n\n**Rejected:**\n- Harden item: rejected.\n'
                                                           + '\n## 2026-01-03 (addendum, owner-directed: the replay above)\n\nThe pr-review replay.\n'
                                                           + 'Filler.\n' * 7 + '**Rejected:**\n- Replay item: rejected.\n')
    for name, when in (('before', -7200), ('inside', 100), ('after', 10 ** 8)):
        p = os.path.join(LESSONS, f'2026-01-01-pr-review-{name}.md'); open(p, 'w').write(name)
        os.utime(p, (t0.timestamp() + when,) * 2)
    repo, bare = os.path.join(tmp, 'repo'), os.path.join(tmp, 'origin.git')
    genv = {**os.environ, 'GIT_CONFIG_GLOBAL': os.devnull, 'GIT_CONFIG_NOSYSTEM': '1'}
    git = lambda *a: subprocess.run(['git', *a], env=genv, check=True, capture_output=True)
    git('init', '-q', '--bare', '-b', 'main', bare); git('clone', '-q', bare, repo)
    os.makedirs(os.path.join(repo, '.claude', 'skills', 'pr-review'))
    open(os.path.join(repo, '.claude', 'skills', 'pr-review', 'SKILL.md'), 'w').write('Run the refuter first.\n')
    git('-C', repo, 'add', '.'); git('-C', repo, '-c', 'user.name=t', '-c', 'user.email=t@example.invalid', 'commit', '-qm', 'one')
    git('-C', repo, 'push', '-q', 'origin', 'main')
    calls, fail_agents, real_run_agent = [], [], run_agent

    def stub(d, brief, model, allow=None):
        calls.append((os.path.relpath(d, MINE_ROOT), allow or READ_ONLY))
        if any(d.endswith(x) for x in fail_agents):
            return d, 'error: stubbed', 0.0
        open(os.path.join(d, 'report.md'), 'w').write('stub report ' + 'x' * 300)
        return d, 'success', 0.0
    run_agent = stub

    stops = []

    def drive(*argv, stop=False):
        sys.argv = ['mine.py', '--repo', repo, *argv]; buf = io.StringIO()
        os.environ['PATH'] = fake + os.pathsep + old
        try:
            with contextlib.redirect_stdout(buf):
                main()
        except SystemExit as e:
            stops.append(str(e))
            if not stop:
                check('the driver runs: ' + ' '.join(argv), str(e), '(no exit)')
        finally:
            os.environ['PATH'] = old
        return buf.getvalue()
    shutil.rmtree(MINE_ROOT); os.makedirs(MINE_ROOT); open(WATERMARK, 'w').write(iso(since) + '\n')
    said = drive('--no-github')
    check('--no-github keeps the watermark and carries nothing', (open(WATERMARK).read().strip(), os.path.exists(CARRIED), 'watermark unchanged: --no-github' in said), (iso(since), False, True))
    D1, D2 = (os.path.join(sorted(glob.glob(os.path.join(MINE_ROOT, '20*')))[0], 'final', x) for x in ('skill-text', 'machinery'))
    rd = lambda p: open(p).read() if os.path.exists(p) else ''
    check('no agent gets a shell, the skill-text merger included', [c for c, a in calls if any('Bash' in x for x in a)], [])
    check('both mergers get the ledger', [os.path.exists(os.path.join(x, 'killed.md')) for x in (D1, D2)], [True, True])
    check('an item six lines under a rejection heading reaches the mergers', 'Item six' in rd(os.path.join(D2, 'killed.md')), True)
    check('the ledger: an owner-directed section about another skill is left out, a pr-review addendum kept',
          ('Harden item' in rd(os.path.join(D2, 'killed.md')), 'Replay item' in rd(os.path.join(D2, 'killed.md'))), (False, True))
    check('the skill-text merger gets every change to the skill', '+Run the refuter first.' in rd(os.path.join(D1, 'pr-review-history-full.txt')), True)
    check('run records: only those written inside the window', sorted(os.listdir(os.path.join(D1, 'runrecords'))) if os.path.isdir(os.path.join(D1, 'runrecords')) else None, ['2026-01-01-pr-review-inside.md'])
    said = drive()
    check('a complete run with outcomes moves the watermark and carries its open PRs',
          (open(WATERMARK).read().strip() != iso(since), 'openmrs/openmrs-core/9999' in (json.load(open(CARRIED)) if os.path.exists(CARRIED) else {})), (True, True))
    said = drive()
    check('an empty window leaves no run folder to resume', (len(glob.glob(os.path.join(MINE_ROOT, '20*'))), 'nothing mined' in said), (2, True))
    keep = os.path.join(tmp, 'keep'); os.makedirs(keep); open(os.path.join(keep, 'notes.txt'), 'w').write('kept')
    drive('--since', '2020-01-01T00:00:00Z', '--until', '2020-01-02T00:00:00Z', '--out', keep)
    check('an empty window keeps what else its --out folder held', sorted(os.listdir(keep)) if os.path.isdir(keep) else None, ['notes.txt'])
    # trap: a re-mine without --out gets a new folder each time, so the hint must name the folder
    fail_agents.append('aux/audit')
    said = drive('--since', iso(since), '--no-github')
    hint = re.search(r'rerun with --out (\S+)', said)
    fail_agents.clear()
    said2 = drive('--out', hint.group(1), '--no-github') if hint else ''
    check('a stopped re-mine names its folder, and rerunning with it resumes its window',
          (bool(hint), said2.split('\n')[0] == said.split('\n')[0], 'Failures' in said2), (True, True, False))
    # trap: run A stops, run B (--out, from the same watermark) completes and moves it; A resumed must not move it back
    open(WATERMARK, 'w').write(iso(since) + '\n')
    fail_agents.append('aux/audit')
    a_out = re.search(r'output (\S+)', drive()).group(1)
    fail_agents.clear()
    w = json.load(open(os.path.join(a_out, 'window.json'))); w['until'] = at(6000); json.dump(w, open(os.path.join(a_out, 'window.json'), 'w'))
    drive('--out', os.path.join(tmp, 'runB'))
    moved = open(WATERMARK).read().strip()
    said = drive()
    check('a resumed run never moves the watermark back', (moved != iso(since), open(WATERMARK).read().strip() == moved, 'another run moved it' in said), (True, True, True))
    # trap: a stage that stops a re-mine must name the folder to resume
    os.environ['FAKE_GH_NO_LOGIN'] = '1'
    said = drive('--since', iso(since), stop=True)
    del os.environ['FAKE_GH_NO_LOGIN']
    check('a stage that stops a re-mine names the folder to resume', ('gave no login' in stops[-1], 'rerun with --out ' + re.search(r'output (\S+)', said).group(1) in stops[-1]), (True, True))
    run_agent = real_run_agent
    shutil.rmtree(tmp)
    print('self-test', 'passed' if ok else 'FAILED')
    sys.exit(0 if ok else 1)


if __name__ == '__main__':
    main()
