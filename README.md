# Functional Programming - Planning a delivery route

This assignment was an exercise in applying ideas form artificial intelligence to the problems of planning an decision-making. The implementation language used was Clojure - a functional programming language we used throughout the year.

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

## License

Copyright © 2016 Kevin Charles

Distributed under the MIT License
