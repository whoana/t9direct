--SELECT count(*) FROM TOP0501 PARTITION (TOP0501_P1);
SELECT max(TRACKING_DATE ) FROM TOP0501 PARTITION (TOP0501_P2);
SELECT count(*) FROM TOP0501 PARTITION (TOP0501_P2);
SELECT count(*) FROM TOP0501 PARTITION (TOP0501_P3);
SELECT count(*) FROM TOP0501 PARTITION (TOP0501_P4);
SELECT count(*) FROM TOP0501 PARTITION (TOP0501_P5);
SELECT count(*) FROM TOP0501 PARTITION (TOP0501_P6);
SELECT count(*) FROM TOP0501 PARTITION (TOP0501_P7);


--SELECT count(*) FROM TOP0503 PARTITION (TOP0503_P1);
SELECT count(*) FROM TOP0503 PARTITION (TOP0503_P2);
SELECT count(*) FROM TOP0503 PARTITION (TOP0503_P3);
SELECT count(*) FROM TOP0503 PARTITION (TOP0503_P4);
SELECT count(*) FROM TOP0503 PARTITION (TOP0503_P5);
SELECT count(*) FROM TOP0503 PARTITION (TOP0503_P6);
SELECT count(*) FROM TOP0503 PARTITION (TOP0503_P7);

DELETE FROM TOP0501 PARTITION (TOP0501_P2) WHERE TRACKING_DATE <= '20200523081136480480';
DELETE FROM TOP0503 PARTITION (TOP0503_P2) WHERE TRACKING_DATE <= '20200523081136480480';
COMMIT;

ALTER TABLE TOP0501 DROP PARTITION TOP0501_P1 ;
ALTER TABLE TOP0503 DROP PARTITION TOP0503_P1 ;

-- 요걸 해줘야 다시 insert 가능하다. 
ALTER INDEX IIPDMC.PK_TOP0501 REBUILD;
ALTER INDEX IIPDMC.UK_TOP0503 REBUILD;


SELECT * FROM DBA_INDEXES 
WHERE INDEX_NAME like '%TOP050%' AND status  = 'UNUSABLE'; 

