#!/opt/homebrew/opt/bash/bin/bash
# Launch the wave-3 reruns once the five-hour usage window has reset (15:40 UTC = 1795707600) plus a minute.
P=/private/tmp/claude-501/-Users-danielkayiwa-Projects-openmrs-chartsearchai/7abec84a-e793-4dfb-aa6b-79a4f7a18e1d/scratchpad/probe-design
until [ "$(date -u +%s)" -ge 1795707660 ]; do sleep 20; done
echo "launching $(date -u +%H:%M:%S)" > $P/waiter3b.txt
exec /opt/homebrew/opt/bash/bin/bash $P/launch3b.sh
