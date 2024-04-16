-- delete user
CREATE OR REPLACE LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION delete_user_function()
	RETURNS "trigger" AS
$BODY$
BEGIN
	DELETE FROM Orders WHERE Orders.customerID = OLD.userID;
	RETURN OLD;
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

DROP TRIGGER IF EXISTS delete_user on Users;
CREATE TRIGGER delete_user BEFORE DELETE
ON Users FOR EACH ROW
EXECUTE PROCEDURE delete_user_function();


-- delete store
CREATE OR REPLACE LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION delete_store_function()
	RETURNS "trigger" AS
$BODY$
BEGIN
	DELETE FROM Orders WHERE Orders.storeID = OLD.storeID;
	DELETE FROM ProductUpdates WHERE ProductUpdates.storeID = OLD.storeID;
	DELETE FROM Product WHERE Product.storeID = OLD.storeID;
	DELETE FROM ProductSupplyRequests WHERE ProductSupplyRequests.storeID = OLD.storeID;
	RETURN OLD;
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

DROP TRIGGER IF EXISTS delete_store on Store;
CREATE TRIGGER delete_store BEFORE DELETE
ON Store FOR EACH ROW
EXECUTE PROCEDURE delete_store_function();


-- delete Warehouse
CREATE OR REPLACE LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION delete_warehouse_function()
	RETURNS "trigger" AS
$BODY$
BEGIN
	DELETE FROM ProductSupplyRequests WHERE ProductSupplyRequests.warehouseID = OLD.WarehouseID;
	RETURN OLD;
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

DROP TRIGGER IF EXISTS delete_warehouse on Warehouse;
CREATE TRIGGER delete_warehouse BEFORE DELETE
ON Warehouse FOR EACH ROW
EXECUTE PROCEDURE delete_warehouse_function();

-- place order trigger
CREATE OR REPLACE LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION place_order_trigger_function()
	RETURNS "trigger" AS
$BODY$
BEGIN
	UPDATE Product
	SET numberOfUnits = numberOfUnits - NEW.unitsOrdered
	WHERE Product.productName = NEW.productName AND Product.storeID = NEW.storeID;
	RETURN NEW;
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

DROP TRIGGER IF EXISTS delete_warehouse on Orders;
CREATE TRIGGER place_order_trigger AFTER INSERT
ON Orders FOR EACH ROW
EXECUTE PROCEDURE place_order_trigger_function();


-- supply request trigger
CREATE OR REPLACE LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION supply_request_trigger_function()
	RETURNS "trigger" AS
$BODY$
BEGIN
	UPDATE Product
	SET numberOfUnits = numberOfUnits + NEW.unitsRequested
	WHERE Product.productName = NEW.productName AND Product.storeID = NEW.storeID;
	RETURN NEW;
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

DROP TRIGGER IF EXISTS supply_request_trigger on ProductSupplyRequests;
CREATE TRIGGER supply_request_trigger AFTER INSERT
ON ProductSupplyRequests FOR EACH ROW
EXECUTE PROCEDURE supply_request_trigger_function();
