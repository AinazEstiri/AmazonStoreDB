-- Primary Key Indexes 
--     • Good for Admin View Everything function
--     • Also good for many other functions' queries

DROP INDEX IF EXISTS user_id_index;
CREATE INDEX user_id_index
ON Users USING BTREE (userID);

DROP INDEX IF EXISTS store_id_index;
CREATE INDEX store_id_index
ON Store USING BTREE (storeID);

DROP INDEX IF EXISTS product_id_index;
CREATE INDEX product_id_index
ON Product USING BTREE (productName);

DROP INDEX IF EXISTS warehouse_id_index;
CREATE INDEX warehouse_id_index
ON Warehouse USING BTREE (WarehouseID);

DROP INDEX IF EXISTS orders_orderNumber_index;
CREATE INDEX orders_orderNumber_index
ON Orders USING BTREE (orderNumber);

DROP INDEX IF EXISTS supply_request_number_index;
CREATE INDEX supply_request_number_index
ON ProductSupplyRequests USING BTREE (requestNumber);

DROP INDEX IF EXISTS product_update_number_index;
CREATE INDEX product_update_number_index
ON ProductUpdates USING BTREE (updateNumber);

-- Other Indexes

-- for selecting or updating product by storeID
DROP INDEX IF EXISTS product_store_id_index;
CREATE INDEX product_store_id_index
ON Product USING BTREE (storeID);


