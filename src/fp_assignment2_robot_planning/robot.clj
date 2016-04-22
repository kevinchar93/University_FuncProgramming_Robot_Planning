(ns fp-assignment2-robot-planning.robot
  (use [fp-assignment2-robot-planning.core])
  (use [clojure.pprint])
  (require [clojure.pprint :as pp])
  (require [clojure.data.priority-map :as pm]))

;-------------------------------------------------------------------------------
; define a record to represent a parcel
(defrecord Parcel [origin-room            ; the room the parcel is originally in
                   dest-room              ; the destination the parcel is going to
                   content                ; the parcel's content
                   delivered])            ; whether the parcel has been delevered or not




;-------------------------------------------------------------------------------
; define a record to represent a robot
(defrecord Robot [graph          ; the graph which represents the building plan
                  curr-pos       ; the current position of the robot
                  parcels        ; the parcels the robot is carrying
                  parcel-map     ; the information about where parcels are in the building
                  curr-path      ; the path the the robot is on currently
                  deliveries     ; map containing which rooms parcels are to be delivered to and how many
                  collections    ; map containing which rooms parcels are to be collected from and how many
                  ]) 



;-------------------------------------------------------------------------------
(defn pn [arg]
  "Shorter name for pretty print"
  (pp/pprint arg))



;-------------------------------------------------------------------------------
(defn pickup-parcels [robot]
  "Picks up all parcels at the robot's current location, updates parcel list and parcel map accordingly"
  
  (let [robot-pos (:curr-pos robot)                         ; get the robot's current positon
        parcel-map (:parcel-map robot)                      ; get the robot's parcel-map
        parcels-pos (robot-pos parcel-map)                  ; use robot-pos to get all the parcels in the map at that position
        
        undelivered (parcels-by-deliv false                 ; get all the undelivered parcels at the current position
                                              parcels-pos)
        remaining (parcels-by-deliv true                    ; get all the remaining parcels that have been delivered already
                                              parcels-pos)
        parcel-map* (assoc parcel-map robot-pos remaining)  ; create a parcel map with collected parcels in 'undelivered' removed
        parcels (:parcels robot)                            ; get the robot's original parcel list
        
        robot+ (if (empty? undelivered)                     ; check to make sure we have undelivered parcels to add
                 robot                                      ; robot remains unchanged
                 (assoc robot :parcels (apply conj          ; update the original 'robot' parcel list (parcels) with the list... 
                                             parcels        ; of undelivered parcels in 'undelivered'
                                             undelivered)))   
        
        robot++ (assoc robot+ :parcel-map parcel-map*)]     ; update the modified 'robot+' parcel-map to change it to the...
                                                            ; updated version 'parcel-map*'
                                                           
    robot++))                                               ; return the newly updated robot



;-------------------------------------------------------------------------------
(defn dropoff-parcels [robot]
  "Drops off all parcels at the robot's current location, updates parcel list and parcel map accordingly"
  
   (let [pos (:curr-pos robot)                            ; get the robot's current positon
         par-map (:parcel-map robot)                      ; get the robot's parcel-map
         parcels (:parcels robot)                         ; get the robot's list of parcels it is carrying
         
         filtered (for [p parcels                         ; filter the parcel list to only get parcels who's destination...
                        :when (= pos                      ; is the current position of the robot
                                 (:dest-room p))]
                    (assoc p                              ; modify the filtered list to set each parcel's delivered flag to true
                            :delivered
                            true))
         remaining (for [p parcels                        ; get the remaining parcels by filtering and getting parcels ...
                         :when (not (= pos                ; who's destination are not where the robot currently is
                                       (:dest-room p)))]
                     p)
         par-map* (add-parcels-to-reg par-map             ; add newly delivered parcels to the parcel map
                                      filtered)
         robot (assoc robot :parcels remaining)           ; update robot's parcel list to the remaining parcels in 'remaining'
         robot (assoc robot :parcel-map par-map*)]        ; update robot's parcel-map to newly update parcel map
     robot))



;-------------------------------------------------------------------------------
(defn cost-to-parcel [flag pos parcel graph]
  "Calculate the cost of travelling from 'pos' to destination of 'parcel'"
  (let [dest (flag parcel)                ; get the destination of the parcel
        path (plan-path graph pos dest)   ; plan a path from 'pos' to the parcels destination
        dist (path-cost graph path)]      ; get the cost of the path
    dist))




;-------------------------------------------------------------------------------
(defn weigh-parcels [flag pos parcels graph]
  "Map the parcels to thier distance from 'pos'"
  (let [weight-map1 (for [p parcels                           ; create first stage of the weight map, putting parcels into...
                          :let [dist (cost-to-parcel flag     ; paired vectors with the cost of a path from 'pos' to 'p'
                                                     pos
                                                     p 
                                                     graph)]]
                      [p dist])
        weight-map2 (apply pm/priority-map                    ; take the first stage of the weight map and put it into a...
                           (flatten weight-map1))]            ; priority map that sorts based on vals
    weight-map2))




;-------------------------------------------------------------------------------
(defn plan-collection-path [init-pos parcel-map graph]
  "Plan a path the robot will follow to collect parcels.
 
   Returns [collection-path  collection-points-map  parcel-delivery-list]"
  
  (loop [pos init-pos                                   ; init as current position of the robot (on recur will be end of total-path)
         parcels (parcels-by-deliv false                ; init the parcels list to have all undelivered parcels
                                  parcel-map)
         total-path (conj [] init-pos)                  ; init the total-path's first node, which the robot's current positon
         coll-points {}                                 ; map stores the rooms parcels are to be collected from, init to empty
         to-deliver nil]                                ; to-deliver list of parcels begins empty
    
    (if parcels                                         ; keep looping over list of parcels until exhausted 
      (let [par-weight-map (weigh-parcels :origin-room  ; map the parcels to the cost to collect them, use as a "to collect" list
                                          pos        
                                          parcels
                                          graph) 
           [curr-parcel _ ] (first par-weight-map)      ; get the parcel closest to the current position (pos)
           loc (:origin-room curr-parcel)               ; get the pick-up location for the current parcel
           
           count (get coll-points loc)                  ; get the ammount of parcels that are to be collected at loc
           coll-points* (if count                       ; incremet the ammout of parcels to collect at loc
                          (assoc coll-points
                                 loc
                                 (inc count))
                          (assoc coll-points loc 1))
           
           plan (plan-path graph pos loc)               ; plan a path from the current position in the plan to loc
           r-plan (subvec plan 1)                       ; getting the plan minus the first node in it
          
           total-path* (if (not (empty? r-plan))        ; append r-plan to the total-path if there is nothing in r-plan...
                         (apply conj                    ; then the total-path remains unchanged
                               total-path 
                               r-plan)
                         total-path)
           pos* (last total-path*)                      ; update the current position to be at the end of the generated path
           par-weight-map* (dissoc par-weight-map       ; get the new parcel map with the current parcel removed from it
                                   curr-parcel)
           parcels* (keys par-weight-map*)              ; get the new list of parcels from par-weight-map*
           to-deliver* (conj to-deliver curr-parcel)]   ; add the current parcel to the 'to-deliver' list
       
        (recur pos*                                     ; recur and rebind new arguments for position, parcel list the total...
               parcels*                                 ; total generated path, collection points and the to deliver list
               total-path*
               coll-points*
               to-deliver*))
      
      [total-path                                       ; return a vector containing the collection path, a map of the locations...
       coll-points                                      ; the parcels are to be collected from and the ammout to collect from...
       to-deliver])))                                   ; said location and the list of parcels to deliver




;-------------------------------------------------------------------------------
(defn plan-delivery-path [to-deliver partial-path graph]
  "Continue a planned collection path by taking a list of deliveries to be made 
   and planning a path to make these deliveries.

   Returns [full-collect-deliver-path  delivery-points]"
  
  (loop [pos (last partial-path)                        ; position begins from where other partial path ended
         
         parcels to-deliver                             ; parcel list that is iterated over is list of parcels to deliver...
                                                        ; created in a previos stage
                                                        
         deliv-points {}                                ; map stores the rooms parcels are to be delivered to, init to empty
         
         total-path partial-path]                       ; the total-path is initialised from the partial path, nodes are..
                                                        ; added to this
    
    (if parcels                                         ; keep looping over list of parcels until exhausted 
      (let [par-weight-map (weigh-parcels :dest-room    ; map the parcels to the cost to deliver them, use as a "to deliver" list
                                          pos        
                                          parcels
                                          graph) 
           [curr-parcel _ ] (first par-weight-map)      ; get the parcel closest to the current position (pos)
           dest (:dest-room curr-parcel)                ; get the destination for the current parcel
           
           count (get deliv-points dest)                ; get the ammount of parcels that are to be delivered at dest
           deliv-points* (if count                      ; incremet the ammout of parcels to collect at dest
                           (assoc deliv-points
                                  dest
                                  (inc count))
                           (assoc deliv-points dest 1))
           
           plan (plan-path graph pos dest)              ; plan a path from the current position in the plan to dest
           r-plan (subvec plan 1)                       ; getting the plan minus the first node in it
          
           total-path* (if (not (empty? r-plan))        ; append r-plan to the total-path if there is nothing in r-plan...
                         (apply conj                    ; then the total-path remains unchanged
                               total-path 
                               r-plan)
                         total-path)
           pos* (last total-path*)                      ; update the current position to be at the end of the generated path
           par-weight-map* (dissoc par-weight-map       ; get the new parcel map with the current parcel removed from it
                                   curr-parcel)
           parcels* (keys par-weight-map*)              ; get the new list of parcels from par-weight-map*
           ] 
       
        (recur pos*                                     ; recur and rebind new arguments for position, parcel list...
               parcels*                                 ; and the total generated path
               deliv-points*
               total-path*))
      
      [total-path                                       ; return the total planned path and the map containing the locations...
       deliv-points])))                                 ; the parcels are to be delivered to and the ammount to deliver to...
                                                        ; said location



                                                        
;-------------------------------------------------------------------------------     
(defn plan-full-route [robot]
  "Plans a full route the robot can uses to collect and deliver all 
   the parcels in its parcel map"
  
  (let [r-graph (:graph robot)                        ; get the graph the robot uses as a map
        r-pos (:curr-pos robot)                       ; get the current position of the robot
        r-parcel-map (:parcel-map robot)              ; get the parcet map from the robot

        [coll-path                                    ; destructure result from 'plan-collection-path'
         coll-points
         to-deliver
         ] (plan-collection-path r-pos                ; use details from the robot to plan a parcel collection path
                                 r-parcel-map
                                 r-graph)
        [full-path                                    ; destructure result from 'plan-collection-path'
         deliv-points
         ] (plan-delivery-path to-deliver             ; use the collection path and the list of parcels in 'to-deliver'...
                               coll-path              ; to create a fully planned path the robot can use to coll/deliv
                               r-graph)
        
        robot (assoc robot :curr-path full-path)      ; update the robot's current path to newly planned path
        robot (assoc robot :deliveries deliv-points)  ; update the robot's deliveries map to delivery points gathered
        robot (assoc robot :collections coll-points)  ; update the robot's collections map to collection points gathered
        ]
    robot))



;-------------------------------------------------------------------------------
(defn print-robot-graph [robot]
  "Print out the graph that the robot uses as the floor plan"
  (let [graph (:graph robot)]
    (println "==================================================")
    (println "Floor Plan")
    (println "==================================================")
    (pn graph)
    (println "==================================================")))




;-------------------------------------------------------------------------------
(defn print-key-robot-data [robot]
  "Print out all the smaller but important information about the robot"
  (let [pos (:curr-pos robot)
        parcels (:parcels robot)
        par-map (:parcel-map robot)
        path (:curr-path robot)
        deliv (:deliveries robot)
        coll (:collections robot)]
    (println "==================================================")
    (println "Key robot infromation")
    (println "==================================================")
    (println "Current position" pos)
    (println "--- Parcels ------------------")
    (print-table [:dest-room :origin-room :content :delivered] (sort-by :dest-room parcels))
    (println)
    (println "--- Parcel Map ---------------")
    (pn par-map)
    (println)
    (println "--- Current Path -------------")
    (pn path)
    (println)
    (println "--- Collections Path ---------")
    (pn coll)
    (println)
    (println "--- Deliveries Map -----------")
    (pn deliv)
    (println)
    (println "==================================================")))




;-------------------------------------------------------------------------------
(defn do-delivery-run [origin-robot]
  (let [origin-robot (plan-full-route origin-robot)] ; plan a path for the delivery run
    
    (print-robot-graph origin-robot)                 ; print the graph representing the floor map
    
    (loop [robot origin-robot]                       ; robot is initialised to 'origin-robot' gets the same path plan
      (let [path (:curr-path robot)                  ; get the path plan from the robot
            coll-map (:collections robot)            ; get the collections map from the robot
            deliv-map (:deliveries robot)            ; get the deliveries map from the robot
            pos (first path)                         ; the current positon of the robot is the first node in the path
            path* (rest path)                        ; the new path is the previous path without the first node
            
            robot (assoc robot                       ; update the current positon of the robot
                         :curr-pos
                         pos)
            
            robot (if (contains? coll-map pos)       ; check if the robot's current positon is in the collections map if so...
                    (pickup-parcels robot)           ; pick up any parcels at that location
                    robot)
            
            robot (if (contains? deliv-map pos)      ; check if the robot's current position is in the delivery map if so...
                    (dropoff-parcels robot)          ; drop off any parcels destined for that location
                    robot)
            
            robot* (assoc robot                      ; update the current path of the robot
                          :curr-path
                          path*)]
        
        (print-key-robot-data robot)                 ; print the state of the robot before we updated the current path
        
        (if (empty? path*)                           ; when path* is empty the robot has reached the end of the path so...
          "DONE"                                     ; signal done
          (recur robot*))))))                        ; rebind with the modified robot record



;-------------------------------------------------------------------------------
; define where all parcels currently are and where ther are to be delivered
;                        |origin|dest|content|delivered|


; parcel lists for each task in the assignment brief
(def task1 [(Parcel. :main-office :r131 "Book" false)])

(def task2 [(Parcel. :main-office :r119 "Folder" false)])

(def task3 [(Parcel. :r113 :r115 "Letter" false)])

(def task4 [(Parcel. :r113 :r129 "Notepad" false)])

(def task5 [(Parcel. :main-office :r131 "Staples" false)
            (Parcel. :r131 :main-office "Pencils" false)])

(def task6 [(Parcel. :main-office :r131 "Notepad" false)
            (Parcel. :main-office :r111 "Book" false)])

(def task6 [(Parcel. :main-office :r131 "Erasers" false)
            (Parcel. :main-office :r111 "CDs" false)])

(def task7 [(Parcel. :main-office :r131 "Plastic Wallets" false)
            (Parcel. :main-office :r111 "CDs" false)
            (Parcel. :r121 :main-office "Pens" false)])

(def task8 [(Parcel. :main-office :r131 "Paper" false)
            (Parcel. :main-office :r111 "USBs" false)
            (Parcel. :r121 :main-office "Folders" false)])



;-------------------------------------------------------------------------------
; define what the current task is
(def curr-task task1)


;-------------------------------------------------------------------------------
; define the initial position of the robot
(def initial-pos :c101)


;-------------------------------------------------------------------------------
; init information about the simulated robot
(def ROBOT (Robot. building
                   initial-pos                         ; set initial position of robot, defined above
                   nil                                 ; parcels list starts out empty
                   (add-parcels-to-reg {} curr-task) ; add all the parcels in 'all-parcels' to the robot's parcel map
                   []                                  ; robot's path is initially empty
                   {}                                  ; map for deliveries is initially empty
                   {}))                                ; map for collections is initially empty




;-------------------------------------------------------------------------------
; Plan a delivery route fof the robot and print the state of the robot along the route
(do-delivery-run ROBOT)

; Plan a delivery route for the robot and print that route to the REPL
(:curr-path (plan-full-route ROBOT))
