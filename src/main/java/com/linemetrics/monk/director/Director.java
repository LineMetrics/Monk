package com.linemetrics.monk.director;


import com.linemetrics.monk.config.dao.DirectorJob;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

public class Director {

    static Map<Integer, DirectorJob> jobs;

    public static void setDirectorJobs(List<DirectorJob> jobs) {
        Director.jobs = new HashMap<>();
        for(DirectorJob directorJob : jobs) {
            Director.jobs.put(directorJob.getId(), directorJob);
        }
    }

    public static DirectorJob getDirectorJobById(int directorJobId) {
        return Director.jobs.get(directorJobId);
    }

    public void run() {
        try {
            SchedulerFactory sf = new StdSchedulerFactory("quartz.properties");
            org.quartz.Scheduler sched = sf.getScheduler();

            for(DirectorJob directorJob : Director.jobs.values()) {

                JobDetail job = JobBuilder.newJob(DirectorInitiator.class)
                        .withIdentity("job_" + directorJob.getId(), "directorjob")
                        .usingJobData("job", directorJob.getId())
                        .build();

                Trigger trigger;
                if(directorJob.getSchedulerMask().equalsIgnoreCase("NOW")) {
                    trigger = TriggerBuilder.newTrigger()
                        .withIdentity("trigger_" + directorJob.getId(), "directorjob")
                        .withSchedule(simpleSchedule()
                            .withRepeatCount(0)) // note that 10 repeats will give a total of 11 firings
                        .build();
                } else {
                    trigger = TriggerBuilder.newTrigger()
                        .withIdentity("trigger_" + directorJob.getId(), "directorjob")
                        .withSchedule(
                            CronScheduleBuilder
                                .cronSchedule(directorJob.getSchedulerMask())
                                .withMisfireHandlingInstructionFireAndProceed())
                        .build();
                }


                sched.scheduleJob(job, trigger);

                System.out.println("Add " + directorJob);

                sched.start();
            }

            synchronized (this) {
                try {
                    this.wait();
                } catch(InterruptedException iexp) {
                    /**
                     * log interrupt exception
                     */
                }
            }

        } catch(SchedulerException exp) {
            exp.printStackTrace();

            /**
             * @todo do log error
             */

            System.exit(-1);
        }
    }
}
