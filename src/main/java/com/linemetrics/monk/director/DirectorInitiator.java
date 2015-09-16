package com.linemetrics.monk.director;

import com.linemetrics.monk.config.dao.*;
import org.quartz.*;


@DisallowConcurrentExecution
public class DirectorInitiator implements Job {

    DirectorJob                         job           = null;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            JobDataMap dataMap = jobExecutionContext.getJobDetail().getJobDataMap();

            /** Wait for 10s in order to be sure, that data is processed */
            Thread.sleep(10000);

            if( ! dataMap.containsKey("job") ||
                ! (dataMap.get("job") instanceof Integer)) {
                return;
            }

            Integer jobId = (Integer)dataMap.get("job");
            job = Director.getDirectorJobById(jobId);

            RunnerContext ctx = new RunnerContext(
                jobId,
                jobExecutionContext.getScheduledFireTime().getTime() - job.getDurationInMillis(),
                jobExecutionContext.getScheduledFireTime().getTime(),
                job.getBatchSizeInMillis(),
                job.getTimeZone()
            );

            DirectorRunner.getInstance().addContext(ctx);

        } catch(Exception exp) {
            exp.printStackTrace();
            throw new JobExecutionException(exp);
        }
    }
}