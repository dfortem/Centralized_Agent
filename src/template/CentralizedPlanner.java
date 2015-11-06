package template;

import logist.plan.Action;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;

import java.util.ArrayList;
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
        private Task t;
        private Action a;

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
