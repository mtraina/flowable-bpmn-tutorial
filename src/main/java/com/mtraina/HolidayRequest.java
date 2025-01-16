package com.mtraina;

import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.TaskService;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.task.api.Task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class HolidayRequest {

    public static void main(String[] args) {
        final var processEngine = init();

        final var scanner = new Scanner(System.in);

        System.out.println("Who are you?");
        final var employee = scanner.nextLine();

        System.out.println("How many holidays do you want to request?");
        final var nrOfHolidays = Integer.valueOf(scanner.nextLine());

        System.out.println("Why do you need them?");
        final var description = scanner.nextLine();

        final var variables = setProcessVariables(processEngine, employee, nrOfHolidays, description);
        final var tasks = getTasks(processEngine);
        final var task = showRequest(scanner, tasks, processEngine.getTaskService());

        manageApproval(scanner, processEngine.getTaskService(), task);
    }

    private static ProcessEngine init(){
        final var cfg = createConfiguration();
        final var processEngine = cfg.buildProcessEngine();
        final var processDefinition = createProcessDefinition(processEngine);
        return processEngine;
    }

    private static ProcessEngineConfiguration createConfiguration(){
        return new StandaloneProcessEngineConfiguration()
                //.setJdbcUrl("jdbc:h2:mem:flowable;DB_CLOSE_DELAY=-1")
                .setJdbcUrl("jdbc:h2:~/test")
                .setJdbcUsername("sa")
                .setJdbcPassword("")
                .setJdbcDriver("org.h2.Driver")
                .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
    }

    private static ProcessDefinition createProcessDefinition(ProcessEngine processEngine){
        final var repositoryService = processEngine.getRepositoryService();
        final var deployment = repositoryService.createDeployment()
                .addClasspathResource("holiday-request.bpmn20.xml")
                .deploy();

        final var processDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .singleResult();
        System.out.println("Found process definition : " + processDefinition.getName());

        return processDefinition;
    }

    private static Map<String, Object> setProcessVariables(ProcessEngine processEngine, String employee, Integer nrOfHolidays, String description){
        final var runtimeService = processEngine.getRuntimeService();

        final var variables = new HashMap<String, Object>();
        variables.put("employee", employee);
        variables.put("nrOfHolidays", nrOfHolidays);
        variables.put("description", description);
        final var processInstance =
                runtimeService.startProcessInstanceByKey("holidayRequest", variables);
        return variables;
    }

    private static List<Task> getTasks(ProcessEngine processEngine){
        final var taskService = processEngine.getTaskService();
        final var tasks = taskService.createTaskQuery().taskCandidateGroup("managers").list();
        System.out.println("You have " + tasks.size() + " tasks:");
        for (int i=0; i<tasks.size(); i++) {
            System.out.println((i+1) + ") " + tasks.get(i).getName());
        }
        return tasks;
    }

    private static Task showRequest(Scanner scanner, List<Task> tasks, TaskService taskService){
        System.out.println("Which task would you like to complete?");
        final var taskIndex = Integer.valueOf(scanner.nextLine());
        final var task = tasks.get(taskIndex - 1);
        final var processVariables = taskService.getVariables(task.getId());
        System.out.println(processVariables.get("employee") + " wants " +
                processVariables.get("nrOfHolidays") + " of holidays. Do you approve this?");
        return task;
    }

    private static void manageApproval(Scanner scanner, TaskService taskService, Task task){
        boolean approved = scanner.nextLine().toLowerCase().equals("y");
        final var variables = new HashMap<String, Object>();
        variables.put("approved", approved);
        taskService.complete(task.getId(), variables);
    }
}