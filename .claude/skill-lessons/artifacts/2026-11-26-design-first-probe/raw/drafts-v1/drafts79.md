# Your own verified findings, drafted as comments for this round and not yet posted

## A, on `api/src/main/java/org/openmrs/module/stockmanagement/api/impl/StockManagementServiceImpl.java` line 2467

Does anything rely on the order of `total`? The commons-lang3 `Pair` hashes as `left ^ right`, where the old one used `31 * left + right`, so this map's order changes. With `totalBy=LocationStockItem` and stock items 1 to 10 at party 3, `total` comes back as 3,2,1,7,6,5,4,10,9,8 where master gives 3,4,...,10,1,2, and the ticket asks for no behaviour changes.

## B, on `api/src/main/java/org/openmrs/module/stockmanagement/api/impl/StockManagementServiceImpl.java` line 3228

This `!email.endsWith(".")` never changes the result, because the pattern has to end in a letter and so never matches an address ending in a dot. If you want to drop it:

```suggestion
		return email != null && EMAIL_PATTERN.matcher(email).matches();
```
