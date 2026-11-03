"""Rough proxy: does a transcript hold an assistant text block answering harden's integration questions
in ANY words? A block counts when it names at least K of the six question topics. Prints per-file and totals.
Usage: integ_content.py K <file-list>"""
import json, os, re, sys
TOPICS = {
    'upstream': re.compile(r'upstream', re.I),
    'optional': re.compile(r'optional (dep|dependenc)|absent at runtime|dependency (is )?absent', re.I),
    'lifecycle': re.compile(r'scans? before|before (it|I|the slice|we) registers?|registration|lifecycle', re.I),
    'sibling': re.compile(r'sibling', re.I),
    'invariant': re.compile(r'invariant|neighbou?r', re.I),
    'compose': re.compile(r'compos(e|es|ing|ition)|race', re.I),
}
K = int(sys.argv[1])
files = [l.split('\t')[0].strip() for l in open(sys.argv[2]) if l.strip()]
tot = hit_files = read = 0
for p in files:
    if not os.path.isfile(p):
        print('MISSING', p); continue
    read += 1
    best = 0
    for line in open(p, errors='replace'):
        try: r = json.loads(line)
        except ValueError: continue
        m = r.get('message') or {}
        if m.get('role') != 'assistant': continue
        c = m.get('content')
        for b in (c if isinstance(c, list) else []):
            if isinstance(b, dict) and b.get('type') == 'text':
                t = b.get('text', '')
                n = sum(1 for rx in TOPICS.values() if rx.search(t))
                best = max(best, n)
    hit = best >= K
    hit_files += hit
    print('%s\tbest=%d\t%s' % ('YES' if hit else 'no', best, os.path.basename(p)[:60]))
print('read:', read, 'of', len(files), '| with a block naming >=%d of 6 topics:' % K, hit_files)
sys.exit(0 if read else 2)
