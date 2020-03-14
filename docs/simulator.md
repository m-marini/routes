# Simulation

March 13th, 2020
marco.marini@mmarini.org

[TOC]

## Vehicle movement

Moving a vehicle on a section is simplified by limiting the speed of the vehicle to the maximum allowed in the edge, the simulated time limit, the safety distance with any subsequent vehicle and the space limit of the section. Unlimited instantaneous acceleration is assumed.

The simulation must calculate the new vehicle position and the time taken to reach the new position.

The safety distance between one car and the next is determined by

```math
    s = v \cdot t_r + l_v
```

where

- $ v $ the vehicle speed
- $ t_r = 1 s $ the reaction time
- $ l_v = 5m $ the vehicle length

The number of vehicles in the edge is then given by

```math
    n = \frac{l_s}{s} = \frac{l_s}{v \cdot t_r+ l_v}
```

where

- $ l_s $ edge length

The maximum number of vehicles on an edge is when the vehicles are completely stationary

```math
    n_x = \frac{l_s}{l_v}
```

The maximum number of vehicles with the maximum flow instead is given by

```math
    n_n = \frac{l_s}{v_x \cdot t_r+ l_v}
```

with $ v_x $ maximum speed in the edge.

We can calculate the relative degree of clogging of an edge as the ratio between the excess of vehicles with respect to the maximum flow and the maximum excess of the edge:

```math
    \nu = \frac{n - n_n}{n_x -n_n}
```

The vehicle in front of everyone has only the maximum speed, the time limit and the space limit of the edge as constraints so the next position will be

```math
    s_i' = \min (s_i + v_x \Delta t, l_s)
```

and the real travel time will be

```math
    \Delta t' = \frac{s_i' - s_i}{v_x}
```

All previous vehicles will move keeping the safety distance and the time limit therfore

```math
    v = \frac{\Delta s}{\Delta t}\\
    \Delta s + s = s_{i+1}-s_i \\
    \Delta s + v t_r + l_v = s_{i+1}-s_i \\
    \Delta s + \frac{\Delta s}{\Delta t} t_r+l_v = s_{i+1}-s_i \\
    \Delta s = \frac{s_{i+1}-s_i - l_v}{1+\frac{t_r}{\Delta t}} \\
    \Delta s = (s_{i+1}-s_i - l_v) \frac{\Delta t}{\Delta t + t_r}
```

## Simulation process

The simulation phase consists of:

### Movement of vehicles on the edges

- Moving of all vehicles on a edge starting from the one in front of everyone.
- The distance traveled in the given time interval is calculated.
- If the vehicle does not leave the edge, the position of the remaining vehicles is calculated in order of position.
- If the vehicle leaves the edge, the time needed to reach the end of the edge must be calculated
- The vehicle is positioned at the end of the edge
- The positions of the remaining vehicles are calculated based on the time interval elapsed to reach the end of the edge.

### Selection of intersections

We now have a set of edges whose simulation time is different and we have to decide which vehicles can move from each section.

The rule is that among the vehicles that intersect on the same node in the reaction time interval, those on the priority edges have priority and in the case of the same priority, priority is given to vehicles coming from the right.

- We take the edges with incomplete time (routes with outgoing vehicles).
- If there are none, we have completed the partial simulation phase

- We filter the edges with minimal simulation time
- We filter the priority edges and with the next edge free, move the vehicles and repeat the process
- If there are no priority edges with the next free edge, those that return are edhes blocked by priority or clogging, therefore all the simulation times of the edges are changed to the time of the edge with a minimum time different from the current one and the process is repeated.

The simulation is completed by moving any outgoing vehicles to the next section if free.

### Precedence selection

- We consider the edges with expected exit time within the considered reaction time
- Among these, those with higher priority are selected
- Among these we take the one on the right,
- If there is none further to the right (intersection with the same priority), select the one with the shortest simulation time (the first arrived at the intersection).

### Vehicle movement at the end of the section

- The next edge is calculated for the vehicle of the selected edge or is deleted if it returns to the departure.
- The vehicle is moved to the next section if it is free
- The position is calculated for the time given by the difference between the simulation time of the next edge and the outgoing edge
- All the edges with the shortest time are stopped for the time of the selected edge.
- If the edge is busy, all edges with a shorter or equal time are stopped for the time of the outgoing edge.
- The process is repeated again until all the edges have completed the simulation time.

The movement phase generates a new configuration of vehicles over time and a new map of transit times.
