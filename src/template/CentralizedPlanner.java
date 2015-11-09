package template;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;

import java.util.*;

/**
 * Centralized_Agent Created by samsara on 06/11/2015.
 */
public class CentralizedPlanner
{
    public static final int PICKUP = 0;
    public static final int DELIVERY = 1;
    public static final double PROBABILITY = 0.4;

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

    // More Useful for changing order
    private static void removeJob(LinkedList<Job> jobList, int task)
    {
        Iterator<Job> iterator = jobList.listIterator();
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
        City homeCity = vehicle.getCurrentCity();
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

    private Task[] getArray(TaskSet tasks)
    {
        Task[] taskArray = new Task[tasks.size()];
        for (Task task : tasks)
        {
            taskArray[task.id] = task;
        }
        return taskArray;
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
        LinkedList<Job> jobs = new LinkedList<>();
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
        for (Vehicle vehicle : vehicles)
        {
            if (vehicle.id() == vehicleId)
            {
                jobList.add(jobs);
            } else
            {
                jobList.add(new LinkedList<>());
            }
        }
    }

    public void chooseNeighbours()
    {
        //Get a random vehicle
        int referenceVehicleId;
        do
        {
            Random random = new Random(System.currentTimeMillis());
            referenceVehicleId = random.nextInt(vehicles.size() - 1);
        } while (jobList.get(referenceVehicleId).isEmpty());

        Vehicle referenceVehicle = vehicles.get(referenceVehicleId);
        List<Job> referencePlan = jobList.get(referenceVehicleId);
        //Changing vehicle operator
        for (Vehicle vehicle : vehicles)
        {
            if (vehicle != referenceVehicle)
            {
                int newIndex = referencePlan.get(0).getT();
                Task task = tasks[newIndex];
                if (task.weight < vehicle.capacity())
                {
                    changingVehicle(referenceVehicleId, vehicle.id());
                }
            }
        }
        /*Changing task order operator:
        int length = jobList.get(referenceVehicleId).size();
        if (length > 2){
            //TODO For all couple of tasks, interchange them using changeTaskOrder
            ArrayList<LinkedList<Job>> newPlan = null;
            neighbours.add(newPlan);
        }*/
    }

    private void changingVehicle(int referenceIndex, int index)
    {
        ArrayList<LinkedList<Job>> newPlan = deepCopy(jobList);

        List<Job> referencePlan = newPlan.get(referenceIndex);
        Task task = tasks[referencePlan.get(0).getT()];
        removeJob(newPlan, referenceIndex, task.id);

        LinkedList<Job> vehiclePlan = newPlan.get(index);
        vehiclePlan.add(new Job(task.id, PICKUP));
        vehiclePlan.add(new Job(task.id, DELIVERY));
        neighbours.add(newPlan);
    }

    // TODO
    private LinkedList<Job> deepCopySingle(LinkedList<Job> jobs)
    {
        return null;
    }

    // TODO
    private void changingTaskOrder(LinkedList<Job> jobs, double capacity)
    {
        HashSet<LinkedList<Job>> set = new HashSet<>();
        LinkedList<Job> newPlan;
        int index = 0;

        for (Job j : jobs)
        {
            if (j.getA() == PICKUP)
            {
                int task = j.getT();
                newPlan = deepCopySingle(jobs);
                removeJob(newPlan, task);

                insertJob(set, newPlan, task, index, capacity);
            }
            index++;
        }

    }

    private void insertJob(HashSet<LinkedList<Job>> set, LinkedList<Job> plan, int task, int index, double capacity)
    {
        int i = 0;
        double load = 0;
        double taskWeight = tasks[task].weight;

        for (Job j : plan)
        {

            if (((capacity - load) >= taskWeight) && (i != index))
            {
                LinkedList<Job> newPlan = deepCopySingle(plan);
                newPlan.add(i, new Job(task, PICKUP));

                insertDelivery(set, newPlan, task, taskWeight, capacity);
                // TODO make a new complete plan for each list in the set and add to the total set.
            }

            int weight = tasks[j.getT()].weight;

            if (j.getA() == PICKUP)
            {
                load += weight; // Pickup
            } else
            {
                load -= weight; // Delivery
            }
            i++;
        }
    }

    private void insertDelivery(HashSet<LinkedList<Job>> set, LinkedList<Job> Plan, int task,
                                double taskWeight,
                                double
                                        capacity)
    {
        boolean isAfterPickup = false;
        double load = 0;
        int index = 0;

        for (Job j : Plan)
        {
            if (j.getT() == task)
            {
                isAfterPickup = true;
            }

            // current payload
            int weight = tasks[j.getT()].weight;

            if (j.getA() == PICKUP)
            {
                load += weight; // Pickup
            } else
            {
                load -= weight; // Delivery
            }

            if (load > capacity)
            {
                return;
            }

            if (isAfterPickup)
            {
                // make new plan,
                LinkedList<Job> newPlan = deepCopySingle(Plan);
                newPlan.add(index + 1, new Job(task, DELIVERY));
                set.add(newPlan);
            }

            index++;
        }
    }


    private ArrayList<LinkedList<Job>> deepCopy(ArrayList<LinkedList<Job>> initialList)
    {
        ArrayList<LinkedList<Job>> newList = new ArrayList<>();
        for (LinkedList<Job> list : initialList)
        {
            LinkedList<Job> temp = new LinkedList<>();
            for (Job job : list)
            {
                try
                {
                    temp.add(job.clone());
                } catch (CloneNotSupportedException e)
                {
                    System.out.println(e);
                }
            }
            newList.add(temp);
        }
        return newList;
    }

    public List<Plan> getPlan()
    {
        List<Plan> finalList = new ArrayList<>();
        int vehicleID = 0;
        for (LinkedList<Job> plan : jobList)
        {
            //Initialize plan
            City current = vehicles.get(vehicleID).getCurrentCity();
            Plan completePlan = new Plan(current);
            //create correct Plan
            if (plan != null)
            {
                for (Job action : plan)
                {
                    //Get task from action
                    Task currentTask = tasks[action.getT()];
                    //find route to action city
                    if (action.getA() == PICKUP)
                    {
                        for (City city : current.pathTo(currentTask.pickupCity))
                        {
                            completePlan.appendMove(city);
                        }
                        completePlan.appendPickup(currentTask);
                        current = currentTask.pickupCity;
                    } else
                    {
                        for (City city : current.pathTo(currentTask.deliveryCity))
                        {
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
        while (finalList.size() < vehicles.size())
        {
            finalList.add(Plan.EMPTY);
        }
        return finalList;
    }

    public void localChoice()
    {
        ArrayList<ArrayList<LinkedList<Job>>> bestSolutions = new ArrayList<>();
        int minCost = Integer.MAX_VALUE;
        for (ArrayList<LinkedList<Job>> list : neighbours)
        {
            int tempCost = 0;
            int vehicleID = 0;
            for (LinkedList<Job> vehicleJob : list)
            {
                tempCost += computeCost(vehicleJob, vehicles.get(vehicleID));
                vehicleID++;
            }
            if (tempCost <= minCost)
            {
                if (tempCost != minCost)
                {
                    bestSolutions.clear();
                }
                bestSolutions.add(list);
            }
        }
        if (bestSolutions.isEmpty())
        {
            System.out.println("Didn't Find any neighbor solution!");
            return;
        }
        Random random = new Random(System.currentTimeMillis());
        int chosenSolution = random.nextInt(bestSolutions.size());
        ArrayList<LinkedList<Job>> bestSolution = bestSolutions.get(chosenSolution);

        double probability = random.nextDouble();
        if (probability < PROBABILITY)
        {
            jobList = bestSolution;
        }
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

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Job job = (Job) o;

            if (t != job.t) return false;
            return a == job.a;

        }

        @Override
        public int hashCode()
        {
            int result = t;
            result = 31 * result + a;
            return result;
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
        public String toString()
        {
            if (a == PICKUP)
            {
                return ("PICKUP TASK " + t);
            } else
            {
                return ("DELIVER TASK " + t);
            }
        }

        @Override
        protected Job clone() throws CloneNotSupportedException
        {
            return (Job) super.clone();
        }
    }
}
