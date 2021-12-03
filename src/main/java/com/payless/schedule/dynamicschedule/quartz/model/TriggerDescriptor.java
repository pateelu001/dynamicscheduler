package com.payless.schedule.dynamicschedule.quartz.model;

import static java.time.ZoneId.systemDefault;
import static java.util.UUID.randomUUID;
import static org.quartz.CronExpression.isValidExpression;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.springframework.util.StringUtils.isEmpty;

import java.io.Serializable;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.TimeZone;

import org.quartz.JobDataMap;
import org.quartz.Trigger;

import lombok.Data;

@Data
public class TriggerDescriptor implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private String groupId;
	private String groupName;
	private LocalDateTime fireTime;
	private String cron;

	public TriggerDescriptor setGroupId(final String groupId) {
		this.groupId = groupId;
		return this;
	}

	public TriggerDescriptor setGroupName(final String groupName) {
		this.groupName = groupName;
		return this;
	}

	public TriggerDescriptor setFireTime(final LocalDateTime fireTime) {
		this.fireTime = fireTime;
		return this;
	}

	public TriggerDescriptor setCron(final String cron) {
		this.cron = cron;
		return this;
	}

	private String buildName() {
		return isEmpty(groupId) ? randomUUID().toString() : groupId;
	}

	/**
	 * Convenience method for building a Trigger
	 * 
	 * @return the Trigger associated with this descriptor
	 */
	public Trigger buildTrigger() {
		// @formatter:off
		if (!isEmpty(cron)) {
			if (!isValidExpression(cron))
				throw new IllegalArgumentException("Provided expression " + cron + " is not a valid cron expression");
			return newTrigger()
					.withIdentity(buildName(), groupName)
					.withSchedule(cronSchedule(cron)
							.withMisfireHandlingInstructionFireAndProceed()
							.inTimeZone(TimeZone.getTimeZone(systemDefault())))
					.usingJobData("cron", cron)
					.build();
		} else if (!isEmpty(fireTime)) {
			JobDataMap jobDataMap = new JobDataMap();
			jobDataMap.put("fireTime", fireTime);
			return newTrigger()
					.withIdentity(buildName(), groupName)
					.withSchedule(simpleSchedule()
							.withMisfireHandlingInstructionNextWithExistingCount())
					.startAt(Date.from(fireTime.atZone(systemDefault()).toInstant()))
					.usingJobData(jobDataMap)
					.build();
		}
		// @formatter:on
		throw new IllegalStateException("unsupported trigger descriptor " + this);
	}

	/**
	 * 
	 * @param trigger
	 *            the Trigger used to build this descriptor
	 * @return the TriggerDescriptor
	 */
	public static TriggerDescriptor buildDescriptor(Trigger trigger) {
		// @formatter:off
		return new TriggerDescriptor()
				.setGroupId(trigger.getKey().getName())
				.setGroupName(trigger.getKey().getGroup())
				.setFireTime((LocalDateTime) trigger.getJobDataMap().get("fireTime"))
				.setCron(trigger.getJobDataMap().getString("cron"));
		// @formatter:on
	}
}
