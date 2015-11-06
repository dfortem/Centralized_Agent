package template;

//the list of imports
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import logist.LogistSettings;

import logist.Measures;
import logist.behavior.AuctionBehavior;
import logist.behavior.CentralizedBehavior;
import logist.agent.Agent;
import logist.config.Parsers;
import logist.plan.Plan;
import logist.plan.Action;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

import javax.swing.*;

/**
 * A very simple auction agent that assigns all tasks to its first vehicle and
 * handles them sequentially.
 *
 */
@SuppressWarnings("unused")
public class CentralizedTemplate implements CentralizedBehavior {

    private final int TOTAL_ITERATIONS = 10000;

    private Topology topology;
    private TaskDistribution distribution;
    private Agent agent;
    private long timeout_setup;
    private long timeout_plan;
    
    @Override
    public void setup(Topology topology, TaskDistribution distribution,
            Agent agent) {
        
        // this code is used to get the timeouts
        LogistSettings ls = null;
        try {
            ls = Parsers.parseSettings("config//settings_default.xml");
        }
        catch (Exception exc) {
            System.out.println("There was a problem loading the configuration file.");
        }
        
        // the setup method cannot last more than timeout_setup milliseconds
        timeout_setup = ls.get(LogistSettings.TimeoutKey.SETUP);
        // the plan method cannot execute more than timeout_plan milliseconds
        timeout_plan = ls.get(LogistSettings.TimeoutKey.PLAN);
        
        this.topology = topology;
        this.distribution = distribution;
        this.agent = agent;
    }

    @Override
    public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
        long time_start = System.currentTimeMillis();

        List<List<Action>> plans = new ArrayList<>();
        plans = initialPlan(vehicles, tasks);
        int counter = 0;
        do{
            List<List<Action>> oldPlans = new ArrayList<>(plans);
            List<List<List<Action>>> neighborSolutions = chooseNeighbors(oldPlans, vehicles, tasks);
            plans = localChoice(neighborSolutions);
            counter++;
        }while(counter < TOTAL_ITERATIONS);

        long time_end = System.currentTimeMillis();
        long duration = time_end - time_start;
        System.out.println("The plan was generated in "+duration+" milliseconds.");

        return listToPlan(plans, vehicles, tasks);
    }

    private List<List<Action>> initialPlan(List<Vehicle> vehicles, TaskSet tasks) {
        long time_start = System.currentTimeMillis();
        
//		System.out.println("Agent " + agent.id() + " has tasks " + tasks);
        List<Action> planVehicle1 = naivePlan(vehicles.get(0), tasks);

        List<List<Action>> plans = new ArrayList<>();
        plans.add(planVehicle1);
        
        long time_end = System.currentTimeMillis();
        long duration = time_end - time_start;
        System.out.println("The plan was generated in "+duration+" milliseconds.");
        
        return plans;
    }

    private List<Action> naivePlan(Vehicle vehicle, TaskSet tasks) {
        City current = vehicle.getCurrentCity();
        List<Action> plan = new ArrayList<>();

        for (Task task : tasks) {
            plan.add(new Action.Pickup(task));
            plan.add(new Action.Delivery(task));
        }
        return plan;
    }

    private List<Plan> listToPlan(List<List<Action>> plans, List<Vehicle> vehicles, TaskSet tasks){
        List<Plan> finalList = new ArrayList<>();
        int vehicleNb = 0;
        for (List<Action> planList : plans){
            //Initialize plan
            City current = vehicles.get(vehicleNb).getCurrentCity();
            Plan completePlan = new Plan(current);
            //create correct Plan
            for (Action action:planList){
                //Get task Id of action
                Task currentTask = getTask(action, tasks);
                //Get task from ID

                //find route to action city
                if (action.getClass() == Action.Pickup.class) {
                    for (City city : current.pathTo(currentTask.pickupCity)) {
                        completePlan.appendMove(city);
                    }
                    completePlan.appendPickup(currentTask);
                    current = currentTask.pickupCity;
                } else {
                    for (City city : current.pathTo(currentTask.deliveryCity)) {
                        completePlan.appendMove(city);
                    }
                    completePlan.appendDelivery(currentTask);
                    current = currentTask.deliveryCity;
                }
            }
            finalList.add(completePlan);
            vehicleNb++;
        }
        while (finalList.size() < vehicles.size()) {
            finalList.add(Plan.EMPTY);
        }
        return finalList;
    }

    private Task getTask(Action action, TaskSet tasks) {
        String task;
        if (action.getClass() == Action.Pickup.class){
            task = action.toString().substring(13, action.toString().length() - 1);
        } else {
            task = action.toString().substring(14, action.toString().length() - 1);
        }
        int taskID = Integer.parseInt(task);
        Task currentTask=null;
        for (Task temporaryTask :tasks){
            if (temporaryTask.id == taskID){
                currentTask = temporaryTask;
                break;
            }
        }
        return currentTask;
    }

    private List<List<List<Action>>> chooseNeighbors(List<List<Action>> oldPlans, List<Vehicle> vehicles, TaskSet tasks) {
        List<List<List<Action>>> neighborSolutions = new ArrayList<>();
        //Get a random vehicle
        int index;
        do {
            Random random = new Random();
            index = random.nextInt(10);
        }while(oldPlans.get(index).isEmpty());
        Vehicle referenceVehicle = vehicles.get(index);
        //Changing vehicle operator
        for (Vehicle vehicle : vehicles){
            if (vehicle != referenceVehicle){
                Task task = getTask(oldPlans.get(vehicle.id()).get(0), tasks);
                if (true /*TODO check capacity*/) {
                    List<List<Action>> newPlan = changeVehicle(vehicle, referenceVehicle, oldPlans);
                    neighborSolutions.add(newPlan);
                }
            }
        }
        //Changing task order operator:
        //TODO Implement chooseNeighbors function
        int length = oldPlans.get(referenceVehicle.id()).size();
        if (length > 2){
            //TODO For all couple of tasks, interchange them using changeTaskOrder
                List<List<Action>> newPlan = null;
                neighborSolutions.add(newPlan);
        }

        return neighborSolutions;
    }

    private List<List<Action>> changeVehicle(Vehicle vehicle, Vehicle referenceVehicle, List<List<Action>> oldPlans)
    {
        List<List<Action>> newPlan = new ArrayList<>();
        //TODO change the first task of referenceVehicle to vehicle
        return newPlan;
    }
    private List<List<Action>> localChoice(List<List<List<Action>>> neighborSolutions){
        List<List<Action>> bestSolutions = new ArrayList<>();
        //TODO Implement localChoice function
        return bestSolutions;
    }
}
