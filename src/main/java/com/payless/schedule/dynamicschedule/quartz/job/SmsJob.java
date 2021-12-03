package com.payless.schedule.dynamicschedule.quartz.job;


import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component
@DisallowConcurrentExecution
@Slf4j
public class SmsJob implements Job {

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap map = context.getMergedJobDataMap();
	
		try{
			
 		String groupId = map.getString("groupId");
		String groupName = map.getString("groupName");
		String timezoneLong = map.getString("timeZoneLong");
       // covidProcess.processCovidNotifications(Integer.parseInt(groupId), timezoneLong , groupName);
	
	   log.info("Job completed"+map.get("timeZoneLong"));
		}catch(Exception e){
			log.error("Unexpected error occured while running job with details "+map.getString("groupId")+ " group name"+map.getString("groupName")+" timezone"+map.getString("timeZoneLong"), e);
		}
	}



	
}