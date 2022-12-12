Parameters: BATCH_001(String), 20221101031334909638(String), HOST_SEND(String), HOST_RCVR4(String), 0(String), RCVR(String),
 20221101031406824095(String), 20221101031406824095(String), 50(Integer), HOST_RCVR4(String), (String), node_os_type(String), null, (String), (String), 10(Integer), 10240(Integer), 0(String), 20221202112401287(String), BATCH_001(String), 20221101031334909638(String), HOST_SEND(String), HOST_RCVR4(String), 0(String), RCVR(String), 20221101031406824095(String), 20221101031406824095(String), 50(Integer), HOST_RCVR4(String), (String), node_os_type(String), null, (String), (String), 10(Integer), 10240(Integer), 0(String), 20221202112401287(String)
2022-12-02 11:24:02.110 ERROR 91542 --- [        loader2] rose.mary.trace.SystemLogger             : resolve exception:SystemError
id:T9E0199999
name:UncategorizedSQLError
type:0
kind:1
msg:statement:
merge into TOP0501 a
using DUAL
on (
        a.INTEGRATION_ID    = 'BATCH_001'
        and a.TRACKING_DATE = '20221101031334909638'                 
        and a.ORG_HOST_ID   = 'HOST_SEND'                 
        and a.PROCESS_ID    = 'HOST_RCVR4'  
)
when matched then
update set                                       
         a.STATUS         = '0'                 
        ,a.NODE_TYPE      = 'RCVR'                 
        ,a.START_DATE     = '20221101031406824095'                 
        ,a.END_DATE       = '20221101031406824095'                 
        ,a.SEQ            = 50                 
        ,a.HOST_ID        = 'HOST_RCVR4'                 
        ,a.IP             = ''                 
        ,a.OS             = 'node_os_type'                 
        ,a.APP_NM         = null                 
        ,a.ERROR_CD       = ''                 
        ,a.ERROR_MSG      = ''                 
        ,a.RECORD_CNT     = 10                 
        ,a.DATA_AMT       = 10240                 
        ,a.CMP            = '0'                 
        ,a.DIRECTORY      = ''                                                
        ,a.FILE_NM        = ''                                                
        ,a.FILE_SIZE      = 0
        ,a.REG_DATE       = '20221202112401287'     
when not matched then 
insert (             
         INTEGRATION_ID                     
        ,TRACKING_DATE                    
        ,ORG_HOST_ID                      
        ,PROCESS_ID                       
        ,STATUS                           
        ,NODE_TYPE                        
        ,START_DATE                       
        ,END_DATE                         
        ,SEQ                              
        ,HOST_ID                          
        ,IP                               
        ,OS                               
        ,APP_NM                           
        ,ERROR_CD                         
        ,ERROR_MSG                        
        ,RECORD_CNT                       
        ,DATA_AMT                         
        ,CMP                              
        ,DIRECTORY                        
        ,FILE_NM                          
        ,FILE_SIZE                         
        ,REG_DATE                
) values (  
        ,'BATCH_001'
        ,'20221101031334909638'
        ,'HOST_SEND'
        ,'HOST_RCVR4'
        ,'0'
        ,'RCVR'
        ,'20221101031406824095'
        ,'20221101031406824095'
        ,50
        ,'HOST_RCVR4'
        ,''
        ,'node_os_type'
        ,null
        ,''
        ,''
        ,10
        ,10240
        ,'0'
        ,''                               
        ,''                               
        ,0
        ,'20221202112401287'
)
