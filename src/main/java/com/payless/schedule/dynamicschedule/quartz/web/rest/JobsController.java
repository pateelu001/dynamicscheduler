package com.payless.schedule.dynamicschedule.quartz.web.rest;

import static org.springframework.http.HttpStatus.CREATED;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.payless.schedule.dynamicschedule.quartz.model.JobDescriptor;
import com.payless.schedule.dynamicschedule.quartz.service.JobsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/quartz")
@RequiredArgsConstructor
public class JobsController {
	private final JobsService jobService;

	@PostMapping(path = "/groups/{group}/jobs")
	public ResponseEntity<JobDescriptor> createJob(@PathVariable String group, @RequestBody JobDescriptor descriptor) {
		return new ResponseEntity<>(jobService.createJob(group, descriptor), CREATED);
	}

	@GetMapping(path = "/groups/{group}/jobs/{name}")
	public ResponseEntity<JobDescriptor> findJob(@PathVariable String group, @PathVariable String name) {
		return jobService.findJob(group, name)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping(path = "/jobs/all")
	public ResponseEntity<List<JobDescriptor>> findAllJobs() {
		List<JobDescriptor> jobs = jobService.findAllJobs();
		return new ResponseEntity<List<JobDescriptor>>(jobs, HttpStatus.OK);
	}

	@PutMapping(path = "/groups/{group}/jobs/{name}")
	public ResponseEntity<Void> updateJob(@PathVariable String group, @PathVariable String name, @RequestBody JobDescriptor descriptor) {
		jobService.updateJob(group, name, descriptor);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping(path = "/groups/{group}/jobs/{name}")
	public ResponseEntity<Void> deleteJob(@PathVariable String group, @PathVariable String name) {
		jobService.deleteJob(group, name);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping(path = "jobs/all")
	public ResponseEntity<Void> deleteJob() {
		List<JobDescriptor> jobs = jobService.findAllJobs();
        for(JobDescriptor eachJob : jobs){
			jobService.deleteJob(eachJob.getGroupName(), eachJob.getGroupId());
		}
		return ResponseEntity.noContent().build();
	}

	@PatchMapping(path = "/groups/{group}/jobs/{name}/pause")
	public ResponseEntity<Void> pauseJob(@PathVariable String group, @PathVariable String name) {
		jobService.pauseJob(group, name);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping(path = "/groups/{group}/jobs/{name}/resume")
	public ResponseEntity<Void> resumeJob(@PathVariable String group, @PathVariable String name) {
		jobService.resumeJob(group, name);
		return ResponseEntity.noContent().build();
	}
}
