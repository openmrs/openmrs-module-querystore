# Module playbook: HTML Form Entry (`htmlformentry`)

Module-specific notes for verifying a UI change in HTML Form Entry. The generic flow lives in `SKILL.md`; this file fills in the two module-specific spots it points to (lightweight rendering in step 5, submit + persistence in step 7).

## Lightweight rendering — preview a form with no patient or saved form

`/openmrs/module/htmlformentry/htmlFormFromFile.form` renders an **uploaded** form XML in ENTER mode against a *fake person* (`HtmlFormEntryUtil.getFakePerson()`) — no patient, no saved `HtmlForm`, just the widget rendered exactly as in a real form. Requires the "Preview Forms" / "Manage Forms" privilege (admin has it).

Drive it in the browser: log in, GET that URL, `setInputFiles('input[name="htmlFormFile"]', formXmlPath)`, click the submit in `form[enctype="multipart/form-data"]`. The rendered form (with `htmlFormEntry.js` loaded) appears in `${previewHtml}`.

A minimal form that exercises the common date widgets:

```xml
<htmlform>
  <p>Encounter date: <encounterDate/></p>
  <p>Onset date (obs, Date): <obs conceptId="<DATE_CONCEPT_UUID>"/></p>
  <p>Procedure date/time (obs, Datetime): <obs conceptId="<DATETIME_CONCEPT_UUID>"/></p>
</htmlform>
```

Widget DOM facts worth knowing:
- A date widget renders a readonly display input `id="<field>-display"` plus a hidden submit field `id="<field>"`. The datepicker uses `altField: '#<field>'` with `altFormat:'yy-mm-dd'`, so `setDate`/clear updates the hidden submit value automatically.
- Set a value programmatically (display is readonly): `window.setDatePickerValue('#<field>-display', '2026-06-15')`.
- The PR #342 clear button is `<span class="ui-icon ui-icon-close" onclick="clearDatePickerValue('#<field>-display','#<field>')">`.
- `encounterDate default="today"` prefills, so on a form with one obs date the *empty* `-display` input is the obs widget.

## Full save + persistence check

To actually persist, you need a saved `HtmlForm` (there is no REST resource for it — create one via *Administration → Manage HTML Forms*, or POST `htmlForm.form` with `form.name`, `form.version`, `form.encounterType`, `xmlData`; fields sit behind a tabbed UI, so set them via DOM and submit the form element). Dev/test standalones already have demo patients (query the `patient` table). Then enter the form at:

```
/openmrs/module/htmlformentry/htmlFormEntry.form?patientId=<pid>&formId=<fid>
```

(`encounterId=<eid>` opens an existing encounter in edit mode.) The form must create an encounter or submit throws — include `<encounterDate/>` + `<submit/>`.

**Submit gotcha.** The visible submit button is `input.submitButton`; its JS pre-check calls `DWRHtmlFormEntryService.checkIfLoggedIn.dwr` before posting. In some standalones that DWR call returns **403** and the real POST never fires (the page just sits on the entry URL). When already authenticated, submit by posting the form directly — `document.getElementById('htmlform').submit()` — which is exactly what the pre-check's success callback does. Don't mistake the stalled submit for a broken change. A successful save redirects to `patientDashboard.form?patientId=<pid>`.

**Verify in the DB.** A saved obs lands in `obs` (`value_datetime` for a date obs) linked to the new `encounter`. Snapshot `MAX(encounter_id)` before submit; the new encounter is the row whose `encounter_id` is greater than that snapshot. Verified example (PR #342, patient 7, concept 4235):
- Control (set date, save): new encounter → `obs.value_datetime = 2026-06-15`, voided=0.
- Clear (set date, click "×", save): new encounter → **zero** rows for that concept.
