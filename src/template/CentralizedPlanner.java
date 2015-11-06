package template;

import logist.plan.Action;
import logist.plan.Action.Delivery;
import logist.plan.Action.Pickup;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Centralized_Agent Created by samsara on 06/11/2015.
 */
public class CentralizedPlanner
{
    // V0 Job(Task, Action), Job(Task, Action)
    // V1 Job(Task, Action), ...
    // ...
    private ArrayList<LinkedList<Job>> jobList;
    private List<Vehicle> vehicles;

    public CentralizedPlanner(List<Vehicle> vehicles, TaskSet tasks)
    {
        this.jobList = new ArrayList<LinkedList<Job>>(vehicles.size());
        this.vehicles = vehicles;

        selectInitialSolution(tasks);
    }


    /**
     * Remove both pickup and delivery of a task from jobList
     *
     * @param vehicle vehicle number
     * @param t       task to be removed
     */
    private void removeJob(int vehicle, Task t)
    {
        Iterator<Job> iterator = jobList.get(vehicle).listIterator();
        while (iterator.hasNext())
        {
            Job j = iterator.next();
            if (j.getT().equals(t))
            {
                iterator.remove(); // Remove pickup and delivery
            }
        }

    }

    /**
     * Give all the tasks to the biggest vehicle. If there exist some tasks that do not fit for the vehicle, then the
     * problem is unsolvable.
     */
    public void selectInitialSolution(TaskSet tasks)
    {
        // Find biggest vehicle
        int vehicleId = 0;
        int capacity = 0;
        for (Vehicle v : vehicles)
        {
            int temp;
            if ((temp = v.capacity()) > capacity)
            {
                vehicleId = v.id();
                capacity = temp;
            }
        }

        LinkedList<Job> jobs = jobList.get(vehicleId);
        for (Task t : tasks)
        {
            if (t.weight < capacity)
            {
                jobs.add(new Job(t, new Pickup(t)));
                jobs.add(new Job(t, new Delivery(t)));
            } else
            {
                throw new IllegalArgumentException("Task do not fit any vehile");
            }

        }


    }

    // TODO
    public void chooseNeighbours()
    {

    }

    // TODO
    private void changingVehicle()
    {

    }

    // TODO
    private void changingTaskOrder()
    {

    }

    // TODO get plan for vehicle
    public List<Plan> getPlan(int vehicle)
    {
        return null;
    }

    // TODO
    public double localChoice()
    {
        return 0;
    }

    private class Job
    {
        private final Task t;
        private final Action a;

        public Job(Task t, Action a)
        {
            this.t = t;
            this.a = a;
        }

        public Task getT()
        {
            return t;
        }

        public Action getA()
        {
            return a;
        }


    }
}
