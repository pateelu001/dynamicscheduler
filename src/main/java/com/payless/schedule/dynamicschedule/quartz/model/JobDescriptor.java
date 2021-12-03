package com.payless.schedule.dynamicschedule.quartz.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.quartz.JobBuilder.*;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Trigger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.payless.schedule.dynamicschedule.quartz.job.SmsJob;

import lombok.Data;

@Data
public class JobDescriptor {
	private String groupId;
	private String groupName;
	private String timeZoneLong;
	
	private Map<String, Object> data = new LinkedHashMap<>();
	@JsonProperty("triggers")
	private List<TriggerDescriptor> triggerDescriptors = new ArrayList<>();

	

	public JobDescriptor setGroupName(final String groupName) {
		this.groupName = groupName;
		return this;
	}

	public JobDescriptor setGroupId(final String groupId) {
		this.groupId = groupId;
		return this;
	}

	public JobDescriptor setTimeZoneLong(final String timeZoneLong) {
		this.timeZoneLong = timeZoneLong;
		return this;
	}
	
	public JobDescriptor setData(final Map<String, Object> data) {
		this.data = data;
		return this;
	}

	public JobDescriptor setTriggerDescriptors(final List<TriggerDescriptor> triggerDescriptors) {
		this.triggerDescriptors = triggerDescriptors;
		return this;
	}

	/**
	 * Convenience method for building Triggers of Job
	 * 
	 * @return Triggers for this JobDetail
	 */
	@JsonIgnore
	public Set<Trigger> buildTriggers() {
		Set<Trigger> triggers = new LinkedHashSet<>();
		for (TriggerDescriptor triggerDescriptor : triggerDescriptors) {
			triggers.add(triggerDescriptor.buildTrigger());
		}

		return triggers;
	}

	

	/**
	 * Convenience method that builds a JobDetail
	 * 
	 * @return the JobDetail built from this descriptor
	 */
	public JobDetail buildJobDetail() {
		// @formatter:off
		JobDataMap jobDataMap = new JobDataMap(getData());
		jobDataMap.put("groupName", groupName);
		jobDataMap.put("groupId", groupId);
		jobDataMap.put("timeZoneLong", timeZoneLong);
		return newJob(SmsJob.class)
                .withIdentity(getGroupId(), getGroupName())
                .usingJobData(jobDataMap)
                .build();
		// @formatter:on
	}

	/**
	 * Convenience method that builds a descriptor from JobDetail and Trigger(s)
	 * 
	 * @param jobDetail
	 *            the JobDetail instance
	 * @param triggersOfJob
	 *            the Trigger(s) to associate with the Job
	 * @return the JobDescriptor
	 */
	@SuppressWarnings("unchecked")
	public static JobDescriptor buildDescriptor(JobDetail jobDetail, List<? extends Trigger> triggersOfJob) {
		// @formatter:off
		List<TriggerDescriptor> triggerDescriptors = new ArrayList<>();

		for (Trigger trigger : triggersOfJob) {
		    triggerDescriptors.add(TriggerDescriptor.buildDescriptor(trigger));
		}
		
		return new JobDescriptor()
				.setGroupId(jobDetail.getKey().getName())
				.setGroupName(jobDetail.getKey().getGroup())
				.setTimeZoneLong(jobDetail.getJobDataMap().getString("timeZoneLong"))
				// .setData(jobDetail.getJobDataMap().getWrappedMap())
				.setTriggerDescriptors(triggerDescriptors);
		// @formatter:on
	}

	
}
