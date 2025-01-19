package com.mtraina;

import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.test.ConfigurationResource;
import org.flowable.engine.test.Deployment;
import org.flowable.engine.test.FlowableTest;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@FlowableTest
@ConfigurationResource("holiday-request.bpmn20.xml")
class HolidayRequestTest {

    private ProcessEngine processEngine;
    private RuntimeService runtimeService;
    private TaskService taskService;

    @BeforeEach
    void setUp(ProcessEngine processEngine) {
        this.processEngine = processEngine;
        this.runtimeService = processEngine.getRuntimeService();
        this.taskService = processEngine.getTaskService();
    }

    @Test
    @Deployment
    void testSimpleProcess() {
        runtimeService.startProcessInstanceByKey("simpleProcess");

        Task task = taskService.createTaskQuery().singleResult();
        assertEquals("My Task", task.getName());

        taskService.complete(task.getId());
        assertEquals(0, runtimeService.createProcessInstanceQuery().count());
    }
}