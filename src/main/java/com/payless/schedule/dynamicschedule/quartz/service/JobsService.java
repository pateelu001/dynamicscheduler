package com.payless.schedule.dynamicschedule.quartz.service;

import static org.quartz.JobKey.jobKey;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.payless.schedule.dynamicschedule.quartz.model.JobDescriptor;

import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jdk.internal.org.jline.utils.Log;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class JobsService {

	@Autowired
	Scheduler scheduler;
	
	public JobDescriptor createJob(String group, JobDescriptor descriptor) {
		descriptor.setGroupName(group);
		JobDetail jobDetail = descriptor.buildJobDetail();
		Set<Trigger> triggersForJob = descriptor.buildTriggers(); 
		 // there will only be one always
		log.info("About to save job with key - {}", jobDetail.getKey());
		try {
			scheduler.scheduleJob(jobDetail, triggersForJob.iterator().next());
			log.info("Job with key - {} saved sucessfully", jobDetail.getKey());
		} catch (SchedulerException e) {
			log.error("Could not save job with key - {} due to error - {}", jobDetail.getKey(), e.getLocalizedMessage());
			throw new IllegalArgumentException(e.getLocalizedMessage());
		}
		return descriptor;
	}
	
	@Transactional(readOnly = true)
	public Optional<JobDescriptor> findJob(String group, String name) {
		// @formatter:off
		try {
			JobDetail jobDetail = scheduler.getJobDetail(jobKey(name, group));
			if(Objects.nonNull(jobDetail))
				return Optional.of(
						JobDescriptor.buildDescriptor(jobDetail, 
								scheduler.getTriggersOfJob(jobKey(name, group))));
		} catch (SchedulerException e) {
			log.error("Could not find job with key - {}.{} due to error - {}", group, name, e.getLocalizedMessage());
		}
		// @formatter:on
		log.warn("Could not find job with key - {}.{}", group, name);
		return Optional.empty();
	}

	@Transactional(readOnly = true)
	public List<JobDescriptor> findAllJobs() {
		// @formatter:off
		List<JobDescriptor> jobsList = new ArrayList<JobDescriptor>();
		try {
			List<String> jobDetails = scheduler.getJobGroupNames();
			for (String groupName : jobDetails) {

				for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
		   
				 String name = jobKey.getName();
				 String group = jobKey.getGroup();
		   
				 try {
					JobDetail jobDetail = scheduler.getJobDetail(jobKey(name, group));
					if(Objects.nonNull(jobDetail))
						
							jobsList.add(JobDescriptor.buildDescriptor(jobDetail, 
										scheduler.getTriggersOfJob(jobKey(name, group))));
				} catch (SchedulerException e) {
					log.error("Could not find job with key - {}.{} due to error - {}", group, name, e.getLocalizedMessage());
				}
		   
				 }
			   }
			   return jobsList;

		} catch (SchedulerException e) {
			log.error("No jobs found");
		}
		// @formatter:on
		return jobsList;
	}
	
	public void updateJob(String group, String name, JobDescriptor descriptor) {
		try {
			JobDetail oldJobDetail = scheduler.getJobDetail(jobKey(name, group));
			if(Objects.nonNull(oldJobDetail)) {
				JobDataMap jobDataMap = oldJobDetail.getJobDataMap();
				jobDataMap.put("timeZoneLong", descriptor.getTimeZoneLong());
				JobBuilder jb = oldJobDetail.getJobBuilder();
				JobDetail newJobDetail = jb.usingJobData(jobDataMap).storeDurably().build();
				scheduler.addJob(newJobDetail, true);
				log.info("Updated job with key - {}", newJobDetail.getKey());
				return;
			}
			log.warn("Could not find job with key - {}.{} to update", group, name);
		} catch (SchedulerException e) {
			log.error("Could not find job with key - {}.{} to update due to error - {}", group, name, e.getLocalizedMessage());
		}
	}
	
	public void deleteJob(String group, String name) {
		try {
			scheduler.deleteJob(jobKey(name, group));
			log.info("Deleted job with key - {}.{}", group, name);
		} catch (SchedulerException e) {
			log.error("Could not delete job with key - {}.{} due to error - {}", group, name, e.getLocalizedMessage());
		}
	}
	
	public void pauseJob(String group, String name) {
		try {
			scheduler.pauseJob(jobKey(name, group));
			log.info("Paused job with key - {}.{}", group, name);
		} catch (SchedulerException e) {
			log.error("Could not pause job with key - {}.{} due to error - {}", group, name, e.getLocalizedMessage());
		}
	}
	
	public void resumeJob(String group, String name) {
		try {
			scheduler.resumeJob(jobKey(name, group));
			log.info("Resumed job with key - {}.{}", group, name);
		} catch (SchedulerException e) {
			log.error("Could not resume job with key - {}.{} due to error - {}", group, name, e.getLocalizedMessage());
		}
	}
}
