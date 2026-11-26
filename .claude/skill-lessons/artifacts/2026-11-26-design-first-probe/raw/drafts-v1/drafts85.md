# Your own verified findings, drafted as comments for this round and not yet posted

## A, on `api/src/test/java/org/openmrs/module/stockmanagement/api/GlobalPropertyDefaultsTest.java` line 39

Can this class go? It calls core's read methods with defaults of its own, so it never runs a read in this module. When i changed one read's default for each of ten settings (rule batch size 100 to 50, DispensingLogsReport's page size 1000 to 500 and eight more) nothing failed. Testing core's behaviour belongs in core.

## B, on `api/src/main/java/org/openmrs/module/stockmanagement/api/impl/StockManagementServiceImpl.java` line 1246

Should this and negativeStockBalanceAllowed warn on values they cannot parse, the way parseUomPriority does? Core reads them with Boolean.parseBoolean, so yes or 1 quietly becomes false. With allowStockIssueWithoutRequisition=yes every new stock issue without a requisition is rejected and nothing in the log says why. Master is the same, but the ticket asks for exactly this to be logged.

## C, on `api/src/main/java/org/openmrs/module/stockmanagement/api/reporting/impl/StockConsumptionReport.java` line 62

Could this be read once, before the forEach? Inside the loop a malformed value such as 30,5 makes core log an ERROR with a stack trace for each row that gets a consumption rate (200 rows gave 200, master logs none). Hoisted, it logs once per 100-row buffer.

```diff
@@ -35,2 +35,7 @@ public class StockConsumptionReport extends StockItemInventoryReport<StockItemIn
 				stockManagementService.postProcessInventoryResult(inventorySearchFilter, stockInventoryResult);
+				BigDecimal daysInMonth = ModuleConstants.positiveValue(
+				    ModuleConstants.REPORTING_CALCULATIONS_NO_DAYS_IN_MONTH,
+				    Context.getAdministrationService().getGlobalPropertyValue(
+				        ModuleConstants.REPORTING_CALCULATIONS_NO_DAYS_IN_MONTH, BigDecimal.valueOf(30.5)),
+				    BigDecimal.valueOf(30.5));
 				stockInventoryResult.getData().forEach(p -> {
@@ -57,9 +62,4 @@ public class StockConsumptionReport extends StockItemInventoryReport<StockItemIn
 						consumption.setConsumptionRate(consumption.getQuantityConsumed().divide(
-						    BigDecimal.valueOf(dayCount).divide(
-						        ModuleConstants.positiveValue(ModuleConstants.REPORTING_CALCULATIONS_NO_DAYS_IN_MONTH,
-						            Context.getAdministrationService().getGlobalPropertyValue(
-						                ModuleConstants.REPORTING_CALCULATIONS_NO_DAYS_IN_MONTH, BigDecimal.valueOf(30.5)),
-						            BigDecimal.valueOf(30.5)),
-						        5, BigDecimal.ROUND_HALF_EVEN),
-						    5, BigDecimal.ROUND_HALF_EVEN));
+						    BigDecimal.valueOf(dayCount).divide(daysInMonth, 5, BigDecimal.ROUND_HALF_EVEN), 5,
+						    BigDecimal.ROUND_HALF_EVEN));
 					}
```

## E, on `omod/src/main/java/org/openmrs/module/stockmanagement/web/resource/StockItemResource.java` line 168

Could this fall back to 50, the config.xml default, instead of null? For a blank or malformed value parseOptionalInteger returns null and the next line passes it to searchStockItemCommonName, which takes an int, so the name search fails with a NullPointerException (HTTP 500). Master does the same, but this line is being rewritten anyway, and parseOptionalInteger could then go.

```suggestion
		Integer maxIntermediateResult = Context.getAdministrationService()
		        .getGlobalPropertyValue(ModuleConstants.STOCK_ITEM_SEARCH_MAX_INTERMEDIATE_RESULT, 50);
```
