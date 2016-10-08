# Functional Programming - Planning a delivery route

This assignment was an exercise in applying ideas form artificial intelligence to the problems of planning and decision-making. The implementation language used was Clojure - a functional programming language we used throughout the year.

<img align:"center" src="https://github.com/kevinchar93/University_FuncProgramming_Robot_Planning/blob/master/office_floor_2.png" 
alt="Office floor plan" width="653" height="529" border="10" />

The diagram above shows the layout of one floor of an office block. The office has a robot on the floor that is able to navigate around it from room to room, collecting and delivering items. The robot has two modes. It is either:

* Collecting parcels, letters and other items for delivery and taking them to the main office 
* Or, having collected items from the main office, it must deliver them to rooms around the 
floor. 

The assignment was to implement a solution that the robot could use to plan its route around the floor - to demonstrate the efficacy of our solution we needed to complete a range of tasks. These tasks are listed below:

| Tasks                                                                                                                                                                                                                                                                                      |
|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Collect a parcel from the main office and deliver it to R131.                                                                                                                                                                                                                              |
| Collect a parcel from the main office and deliver it to R119.                                                                                                                                                                                                                              |
| Collect a parcel from R113 and deliver it to room R115.                                                                                                                                                                                                                                    |
| Collect a parcel from R113 and deliver it to room R129.                                                                                                                                                                                                                                    |
| Collect a parcel from the main office and deliver it to R131. Collect another parcel from,R131 and deliver it to the main office. This should be planned as a single journey.                                                                                                              |
| Collect two parcels from the main office. Deliver one to R131 and the other to R111. This,should be planned as a single journey.                                                                                                                                                           |
| Collect two parcels from the main office. Deliver one to R131 and the other to R111.,Collect another parcel from R121 and deliver it to the main office. This should be,planned as a single journey.                                                                                       |
| Collect two parcels from the main office. Deliver one to R131 and the other to R111.,Collect another parcel from R121 and deliver it to the main office. This should be planned as multiple journeys with the route recalculated for the second leg once the first leg has been completed. |

## Operational Overview

This problems naturally tends towards a solution that makes use of a graph data structure. So I initially created a simple colour coded graph diagram based on the room floor plan to easily grasp the layout of the room and convert it easily into its computer representation. The graph I created is shown below.


<img src="https://github.com/kevinchar93/University_FuncProgramming_Robot_Planning/blob/master/office_custom_graph.png" 
alt="Office floor plan" width="870" height="800" border="10" />

Following Clojure convention I separated the code into namespaces based on their role in the solution, there is a **data**, **core** and **robot** namespace.

| Namespace | Role                                                                                                                                                                                                                                                                                                                                                                                                  |
|-----------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [data](https://github.com/kevinchar93/University_FuncProgramming_Robot_Planning/blob/master/src/fp_assignment2_robot_planning/data.clj)     | contains the raw data that lists all the rooms in the building and which rooms are connected to one another                                                                                                                                                                                                                                                                                           |
| [core](https://github.com/kevinchar93/University_FuncProgramming_Robot_Planning/blob/master/src/fp_assignment2_robot_planning/core.clj)      | uses the information in the **data** namespace to build up an adjacency list that represents the graph shown earlier. It defines lower level path planning functions that are used to plan paths from any node to any other given node in the floor plan, its also defines functions that are used to operate on the robot's parcel list & parcel map - which are used during delivery.                   |
| [robot](https://github.com/kevinchar93/University_FuncProgramming_Robot_Planning/blob/master/src/fp_assignment2_robot_planning/robot.clj)     | the highest level namespace, contains data structures that represent a robot & parcel, its also defines functions **“plan-full-route”** which plan out a full delivery route for the robot and **“do-delivery-run”** which plan a delivery route and executes the plan, showing the state of the robot at each stage. Functions in this namespace make use of the lower level functions in the **core** namespace |

## License

Copyright © 2016 Kevin Charles

Distributed under the MIT License
