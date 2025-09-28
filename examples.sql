-- Oracle 8i Connector - SQL Examples
-- These are example queries you can send to the /query endpoint

-- 1. SELECT queries (return data)
SELECT * FROM emp WHERE deptno = 10;
SELECT empno, ename, sal FROM emp WHERE sal > 2000;
SELECT COUNT(*) as total_employees FROM emp;
SELECT deptno, COUNT(*) as employees_per_dept FROM emp GROUP BY deptno;

-- 2. INSERT queries (add new data)
INSERT INTO emp (empno, ename, job, mgr, hiredate, sal, comm, deptno) 
VALUES (9999, 'TEST_USER', 'CLERK', 7782, SYSDATE, 2000, NULL, 10);

-- 3. UPDATE queries (modify existing data)
UPDATE emp SET sal = sal * 1.1 WHERE deptno = 10;
UPDATE emp SET job = 'SENIOR_CLERK' WHERE empno = 9999;

-- 4. DELETE queries (remove data)
DELETE FROM emp WHERE empno = 9999;
DELETE FROM emp WHERE deptno = 50 AND sal < 1000;

-- 5. DDL queries (create/modify table structure)
CREATE TABLE test_table (
    id NUMBER PRIMARY KEY,
    name VARCHAR2(50),
    created_date DATE DEFAULT SYSDATE
);

ALTER TABLE test_table ADD description VARCHAR2(200);

DROP TABLE test_table;

-- 6. Complex queries with parameters
-- Use these with the parameters array in the JSON request
SELECT * FROM emp WHERE deptno = ? AND sal > ?;
INSERT INTO emp (empno, ename, job, deptno) VALUES (?, ?, ?, ?);
UPDATE emp SET sal = ? WHERE empno = ?;
DELETE FROM emp WHERE deptno = ? AND sal < ?;
