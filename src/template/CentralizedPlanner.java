package template;

import logist.plan.Action;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Centralized_Agent Created by samsara on 06/11/2015.
 */
public class CentralizedPlanner
{
    public static final int PICKUP = 0;
    public static final int DELIVERY = 1;

    private static Task[] tasks;
    private static List<Vehicle> vehicles;

    private ArrayList<LinkedList<Job>> jobList;
    // V0 Job(Task, Action), Job(Task, Action)
    // V1 Job(Task, Action), ...
    // V2 ...

    private Set<ArrayList<LinkedList<Job>>> neighbours;

    public CentralizedPlanner(List<Vehicle> vehicles, TaskSet tasks)
    {
        this.jobList = new ArrayList<>(vehicles.size());
        CentralizedPlanner.tasks = getArray(tasks);
        CentralizedPlanner.vehicles = vehicles;
        this.neighbours = new HashSet<>();

        selectInitialSolution();
    }

    private Task[] getArray(TaskSet tasks){
        Task[] taskArray = new Task[tasks.size()];
        for (Task task : tasks){
            taskArray[task.id]=task;
        }
        return taskArray;
    }
    /**
     * Remove both pickup and delivery of a task from jobList
     *
     * @param vehicle vehicle number
     * @param task    task to be removed
     */
    private static void removeJob(ArrayList<LinkedList<Job>> jobList, int vehicle, int task)
    {
        Iterator<Job> iterator = jobList.get(vehicle).listIterator();
        while (iterator.hasNext())
        {
            Job j = iterator.next();
            if (j.getT() == task)
            {
                iterator.remove(); // Remove pickup and delivery
            }
        }
    }

    private static double computeCost(LinkedList<Job> jobs, Vehicle vehicle)
    {
        Task t;
        double distance = 0;
        City homeCity = vehicle.homeCity();
        for (Job j : jobs)
        {
            t = tasks[j.getT()];
            City taskCity;
            if (j.getA() == PICKUP)
            {
                taskCity = t.pickupCity;
                distance += taskCity.distanceTo(homeCity);
            } else
            {
                taskCity = t.deliveryCity;
                distance += taskCity.distanceTo(homeCity);
            }
            homeCity = taskCity;
        }
        return distance * vehicle.costPerKm();
    }

    /**
     * Give all the tasks to the biggest vehicle. If there exist some tasks that do not fit for the vehicle, then the
     * problem is unsolvable.
     */
    public void selectInitialSolution()
    {
        // Find biggest vehicle
        int vehicleId = 0;
        int capacity = 0;
        for (Vehicle v : vehicles)
        {
            int temp;                                                          //not necessary, just use v.capacity()
            if ((temp = v.capacity()) > capacity)
            {
                vehicleId = v.id();
                capacity = temp;
            }
        }

        // Add all tasks to one vehicle
        LinkedList<Job> jobs = new LinkedList<Job>();
        for (int i = 0; i < tasks.length; i++)
        {
            if (tasks[i].weight < capacity)
            {
                jobs.add(new Job(i, PICKUP));
                jobs.add(new Job(i, DELIVERY));
            } else
            {
                throw new IllegalArgumentException("Task do not fit any vehicle");
            }
        }
        jobList.add(vehicleId,jobs);
    }

    public void chooseNeighbours()
    {
        //Get a random vehicle
        int index;
        do {
            Random random = new Random();
            index = random.nextInt(vehicles.size()-1);
        }while(jobList.get(index).isEmpty());

        Vehicle referenceVehicle = vehicles.get(index);
        List<Job> referencePlan = jobList.get(index);
        //Changing vehicle operator
        for (Vehicle vehicle : vehicles){
            if (vehicle != referenceVehicle){
                Task task = tasks[referencePlan.get(0).getT()];
                if (task.weight<vehicle.capacity()) {
                    changingVehicle(index, vehicle.id());
                }
            }
        }
        //Changing task order operator:
        int length = jobList.get(index).size();
        if (length > 2){
            //TODO For all couple of tasks, interchange them using changeTaskOrder
            ArrayList<LinkedList<Job>> newPlan = null;
            neighbours.add(newPlan);
        }
    }

    // TODO TAKE CARE OF REMOVEJOB METHOD INSIDE!
    private void changingVehicle(int referenceIndex, int index)
    {
        ArrayList<LinkedList<Job>> newPlan = new ArrayList<>(jobList);

        List<Job> referencePlan = jobList.get(referenceIndex);
        Task task = tasks[referencePlan.get(0).getT()];
        //removeJob(this.jobList,referenceIndex,task.id);

        LinkedList<Job> vehiclePlan = newPlan.get(index);
        vehiclePlan.add(new Job(task.id, PICKUP));
        vehiclePlan.add(new Job(task.id, DELIVERY));
        neighbours.add(newPlan);
    }

    // TODO
    private void changingTaskOrder()
    {

    }

    public List<Plan> getPlan()
    {
        List<Plan> finalList = new ArrayList<>();
        int vehicleID = 0;
        for (LinkedList<Job> plan : jobList){
            //Initialize plan
            City current = vehicles.get(vehicleID).getCurrentCity();
            Plan completePlan = new Plan(current);
            //create correct Plan
            if (plan != null) {
                for (Job action : plan) {
                    //Get task from action
                    Task currentTask = tasks[action.getT()];
                    //find route to action city
                    if (action.getA() == PICKUP) {
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
            }
            finalList.add(completePlan);
            vehicleID++;
        }
        while (finalList.size() < vehicles.size()) {
            finalList.add(Plan.EMPTY);
        }
        return finalList;
    }


    // TODO
    public double localChoice()
    {
        return 0;
    }

    private class Job implements Cloneable
    {
        private final int t; // index of the task in tasks array
        private final int a; // PICKUP or DELIVERY

        public Job(int t, int a)
        {
            this.t = t;
            this.a = a;
        }

        public Job(Job j)
        {
            this.t = j.getT();
            this.a = j.getA();
        }

        public int getT()
        {
            return t;
        }

        public int getA()
        {
            return a;
        }

        @Override
        protected Object clone() throws CloneNotSupportedException
        {
            return super.clone();
        }
    }
}
