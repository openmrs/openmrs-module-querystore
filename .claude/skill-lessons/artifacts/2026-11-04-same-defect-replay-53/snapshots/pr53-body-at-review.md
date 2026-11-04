# (fix) O3-6201: Reject stock actions without an operation UUID

author: denniskigen · base: master · head: d4a9bba8131a84229bccfd05c49fe0dc5f34cce0

## Summary

Reject stock-operation actions with missing, empty or whitespace UUIDs before looking up an operation. Currently a blank UUID removes the search filter, allowing an action to affect the newest visible operation.

Missing UUIDs return 400 using the REST module's bad-request exception, including on REST 3.0. Unknown UUIDs return 404.

## Testing performed

- Targeted Maven verify passes with four resource tests, covering 40 action/blank-UUID combinations, HTTP 400 through the declared REST 3.0 controller, explicit target selection and unknown targets.
- On local REST 3.6, all 15 malformed Complete, Submit and Dispatch requests returned 400; both stored test receipts were unchanged. An unknown UUID returned 404.
- A two-receipt test confirmed that submitting the older receipt by UUID changed only that receipt.

## Related issues

https://openmrs.atlassian.net/browse/O3-6201

Paired [frontend guard](https://github.com/openmrs/openmrs-esm-stock-management-app/pull/697).
