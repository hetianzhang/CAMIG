package org.cloudbus.cloudsim.power;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.jgrapht.Graph;
import org.jgrapht.alg.clique.BronKerboschCliqueFinder;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;

import java.util.*;

public class PowerVmAllocationPolicyMigrationConcurrencyAware extends PowerVmAllocationPolicyMigrationLocalRegression{

    private Graph<String, DefaultEdge> depGraph;
    private Map<Vm, Host> currentVmPlacement = new HashMap<>();
    private Map<Integer, List<? extends Host>> vmPotentialDstMap = new HashMap<>();
    private Map<String, List<Vm>> depGraphVmListMap = new HashMap<>();

    public PowerVmAllocationPolicyMigrationConcurrencyAware(List<? extends Host> hostList, PowerVmSelectionPolicy vmSelectionPolicy, double safetyParameter, double schedulingInterval, PowerVmAllocationPolicyMigrationAbstract fallbackVmAllocationPolicy) {
        super(hostList, vmSelectionPolicy, safetyParameter, schedulingInterval, fallbackVmAllocationPolicy);
        depGraph = new DefaultUndirectedGraph<>(DefaultEdge.class);
    }


    public void addEdge_depGraph() {
        for(String n: depGraph.vertexSet()){
            for(String m: depGraph.vertexSet()){
                if (!n.equals(m)) {
                    String[] nlist = n.split("-", 0);
                    String[] mlist = m.split("-", 0);
                    Integer ns = Integer.valueOf(nlist[0]);
                    Integer nd = Integer.valueOf(nlist[1]);
                    Integer ms = Integer.valueOf(mlist[0]);
                    Integer md = Integer.valueOf(mlist[1]);
                    if (ns.equals(ms) || nd.equals(md)) {
                        //check src-dst pair dependency
                        depGraph.addEdge(n, m);
                    }
                }
            }
        }
    }

    public void createDepGraph(List<? extends Vm> vmList) {
        for(Vm vm: vmList) {
            int vmId = vm.getId();
            Host srcHost = this.getHost(vm);
            this.currentVmPlacement.put(vm, srcHost);
            try {
                List<? extends Host> dstList = this.vmPotentialDstMap.get(vmId);
            for(Host dst : dstList){
                String srcdst = String.valueOf(srcHost.getId()) + '-' + String.valueOf(dst.getId());
                if(!depGraph.containsVertex(srcdst)) {
                    depGraph.addVertex(srcdst);
                }
                //put vm in the vm list of the same src-dst pair node
                List<Vm> srcdstVmList = depGraphVmListMap.get(srcdst);
                if(srcdstVmList == null){
                    srcdstVmList = new ArrayList<>();
                }
                srcdstVmList.add(vm);
                depGraphVmListMap.put(srcdst, srcdstVmList);
            }
            }catch (Exception e){
                System.out.println("Empty list for VM "+vmId);
            }
        }
    }

    /**
     * get the single migration execution time
     * @param memorySize
     * @param dirtyRate
     * @param dtThread
     * @param roundThread
     * @param bw
     * @param compressionRate
     * @return
     */
    public Map<String, Double> getVmMigrationTime(double memorySize, double dirtyRate, double dtThread, int roundThread, double bw, double compressionRate ) {
        double migTime = 0.0;
        double dt;
        double memorySizeBit = memorySize * Math.pow(10,9) * 8;
        double transSize = memorySizeBit * compressionRate;
        double round = 1;
        while(true) {
            if (transSize / bw > dtThread && round <= roundThread) {
                migTime += transSize / bw;
                transSize = dirtyRate * compressionRate * migTime;
                round += 1;
            }
            else {
                dt = transSize/bw;
                migTime += dt;
                break;
            }
        }

        Map<String, Double> resultMap = new HashMap<>();
        resultMap.put("migTime", migTime);
        resultMap.put("downtime", dt);
        return resultMap;
    }

    /**
     * Get normalized migration time for all src-dst combination
     * @param vmList
     */
    public Map<Vm, Double> getNormalizedMigrationTime(List<? extends Vm> vmList) {
        Map<Vm, Double> vmNormalizedTimeMap = new HashMap<>();
        double dirtyRate = 0.3;
        double dtThread = 0.5;
        int roundThread = 30;
        double compressionRate = 0.4;
        double maxMigTime = 0;
        double minMigTime = Double.MAX_VALUE;


        List<Double> tempMigTimeList = new ArrayList<>();
        for(Vm vm:vmList){
            double bw = getHost(vm).getBw();
            Map<String, Double> migResult = getVmMigrationTime(vm.getRam(),dirtyRate,dtThread,roundThread,bw,compressionRate);
            double migTime = migResult.get("migTime");
            tempMigTimeList.add(migTime);
            //double dt = migResult.get("downtime");
            if(migTime > maxMigTime)
                maxMigTime = migTime;
            if(migTime< minMigTime)
                minMigTime = migTime;
        }
        int index = 0;
        for(Double t:tempMigTimeList){
            double normMigTime = (t - minMigTime)/(maxMigTime - minMigTime);
            vmNormalizedTimeMap.put(vmList.get(index), normMigTime);
        }
        return vmNormalizedTimeMap;
    }

    /**
     * Extra Cost Model: Get normalized migration co-location interference for all src-dst combination
     * @param vmList
     */
    public void getColocationInterference(List<? extends Vm> vmList) {

    }

    /**
     * Extra Cost Model: Get normalized migration impact after the scheduling
     * @param vmlist
     */
    public void getImpactMigration(List<? extends Vm> vmlist) {

    }

    /**
     * check if the checking src-dst pair can add to the Independent Set (indepSet)
     * @param indepSet
     * @param srcdstCheck
     * @return
     */
    public boolean addGroup(Set<String> indepSet, String srcdstCheck) {
        if(indepSet == null)
            return true;
        for(String srcdst:indepSet) {
            if(depGraph.containsEdge(srcdst, srcdstCheck)){
                return false;
            }
        }
        return true;
    }

    /**
     * Get all independent Set based on the all maximal cliques of the Graph
     * @param maxcliqList
     * @return
     */
    public List<Set<String>> getAllIndependentSetDepGraph(List<Set<String>> maxcliqList) {
        int totalLen = -1;
        List<Set<String>> allIndepSets = new ArrayList<>();

        List<String> deleteList = new ArrayList<>();
        for(String srcdst : depGraph.vertexSet()){
            for(Set<String> clique:maxcliqList){
                if(clique.contains(srcdst)) {
                    deleteList.addAll(clique);
                }
            }
        }


        List<Set<String>> newCliquesList = new ArrayList<>();
        for(Set<String> clique : maxcliqList) {
            Set<String> updateClique = null;
            for(String srcdst : clique) {
                if(!deleteList.contains(srcdst))
                    updateClique.add(srcdst);
            }
            newCliquesList.add(updateClique);
        }

        List<Set<String>> nodeSetList = new ArrayList<>();
        List<String> srcdstListCopy = new ArrayList<>(depGraph.vertexSet());
        while(srcdstListCopy.size() != 0) {
            Set<String> indepSet = null;
            for(String srcdst: srcdstListCopy) {
                if(addGroup(indepSet, srcdst))
                    indepSet.add(srcdst);
            }
            nodeSetList.add(indepSet);
            List<String> srcdstListCopy1 = new ArrayList<>();
            for(String srcdst:srcdstListCopy){
                if(!indepSet.contains(srcdst))
                    srcdstListCopy1.add(srcdst);
            }
            srcdstListCopy = srcdstListCopy1;
        }

        List<Set<String>> maxcliqListCopy = new ArrayList<>();
        for(Set<String> clique:maxcliqList){
            Set<String> copySet = null;
            for(String srcdst:clique){
                copySet.add(srcdst);
            }
            maxcliqListCopy.add(copySet);
        }
        //create independent set of current migs
        for(Set<String> ind:nodeSetList){
            totalLen = 1;
            System.out.println(ind);
            int oldlen = 0;
            while(totalLen != 0 && oldlen != totalLen) {
                oldlen = totalLen;
                totalLen = 0;
                Set<String> set = null;
                for(String srcdst:ind){
                    set.add(srcdst);
                }
                for(Set<String> clique:maxcliqListCopy){
                    if(clique.size() != 0) {
                        totalLen += clique.size();
                        Set<String> cliqueCopy = null;
                        for(String srcdst: clique){
                            cliqueCopy.add(srcdst);
                        }
                        for(String srcdst: cliqueCopy){
                            if(addGroup(set, srcdst)){
                                set.add(srcdst);
                                clique.remove(srcdst);
                                break;
                            }
                        }
                    }
                }
                allIndepSets.add(set);
            }
        }
        return allIndepSets;
    }


    /**
     * Get all maximal Cliques of the Dependency Graph (depGraph)
     * @return
     */
    public List<Set<String>> getAllCliquesDepGraph(){
        List<Set<String>> maxcliqList = new ArrayList<>();
        BronKerboschCliqueFinder<String, DefaultEdge> finder = new BronKerboschCliqueFinder<>(depGraph);
        finder.iterator();
        for(Iterator<Set<String>> iter = finder.iterator();iter.hasNext();){
            Set<String> clique = iter.next();
            maxcliqList.add(clique);
            for(String srcdst: clique){
            for (String srcdst1 : clique) {
                if(!srcdst.equals(srcdst1))
                if(!depGraph.containsEdge(srcdst, srcdst1)){
                    System.out.print("error clique");
                }
            }
            }
        }
        return maxcliqList;
    }

    /**
     * get all cliques which include the given srcdst Node
     * @param allGraphCliques
     * @param srcdstNode
     * @return
     */
    private List<Set<String>> getAllCliquesForNode(List<Set<String>> allGraphCliques, String srcdstNode){
        List<Set<String>> cliquesForNodeList = new ArrayList<>();
        for(Set<String> clique: allGraphCliques){
            if(clique.contains(srcdstNode)){
                cliquesForNodeList.add(clique);
            }
        }
        return cliquesForNodeList;
    }

    /**
     * get all independent set which include the given srcdst node
     * @param allGraphCliques
     * @param srcdstNode
     * @return
     */
    private List<Set<String>> getAllIndepSetForNode(List<Set<String>> allGraphCliques, String srcdstNode){
        List<Set<String>> allIndepSetForNodeList = new ArrayList<>();
        List<Set<String>> allGraphCliquesCopy = new ArrayList<>();
        for(Set<String> clique: allGraphCliques){
            Set<String> cliqueCopy = new HashSet<>(clique);
            allGraphCliquesCopy.add(cliqueCopy);
        }
        int totalLen = 1;
        int oldLen = 0;
        while (totalLen != 0 && totalLen != oldLen) {
           oldLen = totalLen;
           totalLen = 0;
           Set<String> indSet = new HashSet<>();
           indSet.add(srcdstNode);
           for(Set<String> clique: allGraphCliquesCopy){
               if(clique.size()!= 0){
                   totalLen += clique.size();
                   String removeNode = null;
                   for(String pairNode: clique){
                       if(addGroup(indSet, pairNode)){
                           //String is immutable
                           indSet.add(pairNode);
                           removeNode = pairNode;
                           break;
                       }
                   }
                   if(removeNode!=null)
                       clique.remove(removeNode);
               }
           }
           if(indSet.size()!=1)
               allIndepSetForNodeList.add(indSet);
           indSet.clear();
        }
        return allIndepSetForNodeList;
    }


    /**
     * Get the concurrency score of specific VM based on current selected VM migration and depGraph
     * @param allGraphCliques all cliques of Graph
     * @param srcdstNode the node need to be checked
     * @param selectedMigrationList provide the already selected Migration with host(dst), src, and vm key value
     * @return concurScore + 1/penaltyScore
     */
    public Double getConcurrencyScoreMigration(List<Set<String>> allGraphCliques, String srcdstNode, List<Map<String, Object>> selectedMigrationList) {
        List<Set<String>> allNodeIndepSets = getAllIndepSetForNode(allGraphCliques, srcdstNode);
        List<Set<String>> allNodeCliques = getAllCliquesForNode(allGraphCliques, srcdstNode);

        int indepScore = 0; //how many selected migration src-dst node in the independent set of srcdstNode
        int cliqueScore = 0;
        for(Map<String, Object> mig:selectedMigrationList){
            Host dst = (Host) mig.get("host");
            Vm vm = (Vm) mig.get("vm");
            Host src = getHost(vm);

            String n = src.getId() +"-"+dst.getId();
            for(Set<String> nodeIndep : allNodeIndepSets) {
                if(nodeIndep.contains(n))
                    indepScore +=1;
            }

            for(Set<String> nodeClique : allNodeCliques) {
                if(nodeClique.contains(n))
                    cliqueScore +=1;
            }
        }


        //select the maximum concurScore
        double concurScore = indepScore / (allNodeIndepSets.size() * selectedMigrationList.size());
        //select the minimum penaltyScore if concurScore is 0
        double penaltyScore = cliqueScore /(allGraphCliques.size() * selectedMigrationList.size());

        if(penaltyScore == 0)
            return concurScore;
        return concurScore + 1/penaltyScore;
    }


    private Map<Host, Integer> getPotentialHostHits(Map<Integer, List<? extends Host>> vmPotentialDstMap){
        Map<Host, Integer> potentialHostHits = new HashMap<>();
        for(Integer vmId:vmPotentialDstMap.keySet()){
            List<? extends Host> potentialDstList = vmPotentialDstMap.get(vmId);
            for(Host dst:potentialDstList){
                if(potentialHostHits.containsKey(dst)){
                    Integer hits = potentialHostHits.get(dst);
                    potentialHostHits.put(dst, hits+1);
                }else{
                    potentialHostHits.put(dst, 1);
                }
            }
        }
        return potentialHostHits;
    }

    private Map<Host, Integer> getSelectedHostHits(List<Map<String, Object>> selectedMigList){
        Map<Host, Integer> selectedHostHits = new HashMap<>();
        for(Map<String, Object> selectedMig: selectedMigList){
            Host dst = (Host) selectedMig.get("host");
            Vm vm = (Vm) selectedMig.get("vm");
            if(selectedHostHits.containsKey(dst)){
                Integer hits = selectedHostHits.get(dst);
                hits +=1;
                selectedHostHits.put(dst, hits);
            }else{
                selectedHostHits.put(dst, 1);
            }
        }
        return selectedHostHits;
    }


    @Override
    public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
        //for each round, selected the most minPower VM with the destination that results in minimal dependency among selected VM Migration
        //stop until all VMs' destination has been selected
        //vmid to vm map
        idtoVmMap.clear();
        for(Vm vm:vmList){
            this.idtoVmMap.put(vm.getId(), vm);
        }

        saveAllocation();

        //initialize select migration list
        //selectedMigList store src, dst, vm
        List<Map<String, Object>> selectedMigList = new ArrayList<>();
        List<Vm> toMigVmList = new ArrayList<>();
        List<Vm> minVmList = new ArrayList<>();
        List<PowerHost> excludedUnderUtilizedHosts = new ArrayList<>();
        List<PowerHost> excludedOverUtilizedHosts = new ArrayList<>();

        //get potential destination for this round of the remaining VMs
        optimizeVmAllocationCandidates(toMigVmList);
        //all overUtilizedHosts and underUtilizedHosts
        List<PowerHost> overUtilizedHosts = new ArrayList<>();
        List<Vm> overUtilizedVmsToMigrate = new ArrayList<>();
        List<PowerHost> underUtilizedHosts = new ArrayList<>();
        List<Vm> underUtilizedVmsToMigrate = new ArrayList<>();
        for(Map.Entry<Host, Vm> en:overUtilizedHostVMMap.entrySet()){
            overUtilizedHosts.add((PowerHost)en.getKey());
            overUtilizedVmsToMigrate.add(en.getValue());
        }
        for(Host underHost:underUtilizedHostVMsMap.keySet()){
            underUtilizedHosts.add((PowerHost)underHost);
        }


        while(true){
            double minPower = Double.MAX_VALUE;

            toMigVmList.clear();

            List<Map<Integer, List<? extends Host>>> potentialDstMatrix= this.getAllocationMatrix();
            for(Map<Integer, List<? extends Host>> vmPotentialDst: potentialDstMatrix){
                for(Map.Entry<Integer, List<? extends  Host>> en: vmPotentialDst.entrySet()){
                    //store this round potential dst to vms
                    this.vmPotentialDstMap.put(en.getKey(), en.getValue());
                    //check the power difference
                    Vm vm = this.idtoVmMap.get(en.getKey());
                    PowerHost vmDst = (PowerHost) en.getValue().get(0);
                    if(excludedOverUtilizedHosts.contains(getHost(vm)) || excludedUnderUtilizedHosts.contains(getHost(vm))){
                        continue;
                    }
                    toMigVmList.add(vm);

                    double powerAfterAllocation = getPowerAfterAllocation(vmDst, vm);
                    if(powerAfterAllocation != -1){
                        double powerDiff = powerAfterAllocation - vmDst.getPower();

                        if(powerDiff < minPower){
                            minVmList.clear();
                            minVmList.add(vm);
                            minPower = powerDiff;
                        }else if(powerDiff == minPower){
                            minVmList.add(vm);
                        }
                    }
                }
            }


            //create depgraph based on current VM, src, potential dst list
            //todo: debug this section
            System.out.println("remaining vm to be selected: "+toMigVmList.size());
            if(toMigVmList.size() == 0)
                break;

            if(minVmList.size()>=1){
                createDepGraph(minVmList);

                for(Map<String, Object> mig:selectedMigList){
                    Vm vm = (Vm) mig.get("vm");
                    Host dst = (Host) mig.get("host");
                    Host src = getHost(vm);
                    String srcdst = String.valueOf(src.getId())+'-'+String.valueOf(dst.getId());
                    if(!depGraph.containsVertex(srcdst)) {
                        depGraph.addVertex(srcdst);
                    }
                    //put vm in the vm list of the same src-dst pair node
                    List<Vm> srcdstVmList = depGraphVmListMap.get(srcdst);
                    if(srcdstVmList == null){
                        srcdstVmList = new ArrayList<>();
                    }
                    srcdstVmList.add(vm);
                    depGraphVmListMap.put(srcdst, srcdstVmList);
                }
                addEdge_depGraph();

                List<Set<String>> allGraphCliques = getAllCliquesDepGraph();
                Map<String, Object> selectedVmMig = new HashMap<>();

                //select one of VMs with largest concurrency score regarding the current selected VM migrations
                double maxScore = -1;
                Host selectDst = null;
                Vm selectVm = null;
                for(Vm vm : minVmList){
                    String src = String.valueOf(this.getHost(vm).getId());
                    for(Host dst:this.vmPotentialDstMap.get(vm.getId())){
                        String srcdstNode = src + "-" + dst.getId();
                        double selectorScore = 0;
                        if(selectedMigList.size()==0){
                            List<Set<String>> nodeCliques = getAllCliquesForNode(allGraphCliques, srcdstNode);
                            int totalCliqueLen = 0;
                            for(Set<String> c:nodeCliques)
                                totalCliqueLen += c.size();
                            selectorScore = 1.0/totalCliqueLen;
                        }else{
                            selectorScore = getConcurrencyScoreMigration(allGraphCliques, srcdstNode, selectedMigList);
                        }
                        if(selectorScore > maxScore){
                            maxScore = selectorScore;
                            selectDst = dst;
                            selectVm = vm;
                        }
                    }
                }



                //selectVm = minVmList.get(0);
                //selectDst = this.vmPotentialDstMap.get(selectVm.getId()).get(0);

                //update the vm candidate and data center resources
                if(selectVm != null) {
                    //todo duplicate VMs in vmlist of srchost
                    if(this.underUtilizedHostVMsMap.containsKey(this.getHost(selectVm))){
                        List<Map<String, Object>> underUtilizedGroupMigList = new ArrayList<>();
                        for(Vm vm:this.underUtilizedHostVMsMap.get(getHost(selectVm))){
                            Map<String, Object> mig = new HashMap<>();
                            mig.put("host", selectDst);
                            mig.put("vm", vm);
                            underUtilizedGroupMigList.add(mig);

                            if(selectDst.vmCreate(vm)) {
                                toMigVmList.remove(vm);
                            }else{
                                for(Map<String, Object> deleteMig:underUtilizedGroupMigList){
                                    selectDst.vmDestroy((Vm) deleteMig.get("vm"));
                                }
                                underUtilizedGroupMigList.clear();
                                break;
                            }
                        }
                        if(underUtilizedGroupMigList.size()!=0) {
                            selectedMigList.addAll(underUtilizedGroupMigList);
                        }
                        excludedUnderUtilizedHosts.add((PowerHost) this.getHost(selectVm));

                    }else {


                        toMigVmList.remove(selectVm);
                        if(selectDst.vmCreate(selectVm)){
                            selectedVmMig.put("host", selectDst);
                            selectedVmMig.put("vm", selectVm);
                            selectedMigList.add(selectedVmMig);
                        }
                        excludedOverUtilizedHosts.add((PowerHost) this.getHost(selectVm));
                    }

                }

                List<Vm> selectedVmList = new ArrayList<>();
                System.out.println("new round");
                for(Map<String, Object> mig:selectedMigList){
                    Vm vm = (Vm) mig.get("vm");
                    Host dst = (Host) mig.get("host");
                    System.out.println("Vm:"+vm.getId()+" src: "+getHost(vm).getId() + "dst: "+dst.getId());

                    //check the vm src host
                    if(!getHost(vm).getVmList().contains(vm)){
                        System.out.println("src error");
                    }
                    if(selectedVmList.contains(vm)){
                        System.out.println("vm duplicate error");
                    }
                    selectedVmList.add(vm);
                }
                for(PowerHost overHost: excludedOverUtilizedHosts){
                    if(excludedUnderUtilizedHosts.contains(overHost))
                        System.out.print("under host become over error");
                }

                depGraph = new DefaultUndirectedGraph<>(DefaultEdge.class);
                depGraphVmListMap.clear();
                vmPotentialDstMap.clear();
                //optimizeVmAllocationCandidates(excludedOverUtilizedHosts, excludedUnderUtilizedHosts);
                optimizeVmAllocationCandidates(selectedMigList, overUtilizedHosts,underUtilizedHosts,overUtilizedVmsToMigrate,excludedOverUtilizedHosts, excludedUnderUtilizedHosts);

                minVmList.clear();
            }else{
                break;
            }
            //exit condition


        }


        /*while(true){
            //List<Vm> toMigVmList = new ArrayList<>();
            //get potential destination for all vm, didn't dependent on the input vmList
            double minPower = Double.MAX_VALUE;


            //generate candidate for this round
            optimizeVmAllocationCandidates(vmList);

            //get minVmList
            System.out.println("Remaining VM to be decided:"+this.getAllocationMatrix().size());
            for(Map<Integer, List<? extends Host>> vmPotentialDst: this.getAllocationMatrix()) {
                for(Map.Entry<Integer, List<? extends Host>> en :vmPotentialDst.entrySet()) {
                    this.vmPotentialDstMap.put(en.getKey(), en.getValue());
                    Vm vm = this.idtoVmMap.get(en.getKey());
                    toMigVmList.add(vm);
                    PowerHost vmHost = (PowerHost) en.getValue().get(0);
                    double powerAfterAllocation = getPowerAfterAllocation(vmHost, vm);
                    if (powerAfterAllocation != -1) {
                        double powerDiff = powerAfterAllocation - vmHost.getPower();
                        if(powerDiff < minPower) {
                            minVmList.clear();
                            minVmList.add(vm);
                            minPower = powerDiff;
                        }else if (powerDiff == minPower){
                            minVmList.add(vm);
                        }
                    }
                }
            }

            //select Vm with least hits host
            //for each step select one VM with optimal destination (minimizing concurrency among selected VM and the new one)
            if(minVmList.size() != 0) {
                //minPower Vms
                //choose minimal hits Host for potential destinations and selected VMs destinations
                int minHits = Integer.MAX_VALUE;
                Host minDst = null;
                Vm minVm = null;
                for(Vm vm: minVmList){
                    Map<Host, Integer> selectedHostHitMap = this.getSelectedHostHits(selectedMigList);
                    List<Host> minDstList = new ArrayList<>();
                    for(Host dst: this.vmPotentialDstMap.get(vm.getId())){
                        int selectedHits = 0;
                        if(selectedHostHitMap.containsKey(dst))
                            selectedHits = selectedHostHitMap.get(dst);
                        if(selectedHits < minHits) {
                            minDst = dst;
                            minVm = vm;
                            minHits = selectedHits;
                            minDstList.add(dst);
                        } else if (selectedHits == minHits) {
                            minDstList.add(dst);
                        }
                    }
                }

                Map<Host, Integer> potentialHostHitMap = this.getPotentialHostHits(this.vmPotentialDstMap);

                //How to update the resource
                //PowerHost allocatedHost = findHostForVm(vm, excludedHosts);
                //allocatedHost.vmCreate(vm);
                //allocatedHost.vmDestroy(vm);

                Map<String, Object> minMig = new HashMap<>();
                minMig.put("host", minDst);
                minMig.put("vm", minVm);

                //update available resources
                if(minDst.vmCreate(minVm)) {
                    toMigVmList.remove(minVm);
                    selectedMigList.add(minMig);
                }
            }

            if(toMigVmList.size() == 0)
                break;
        }

        List<Map<String, Object>> migrationMap  = new ArrayList<Map<String, Object>>();
        migrationMap.addAll(selectedMigList);





        //update the system resource


        //until there is no VM left or dynamic resource management target is achieved

        //generate dependency graph based on selected VMs, current source host and potential destinations
        //this.generateDepGraph(toMigVmList);
        //List<Set<String>> maxCliques = this.getAllCliquesDepGraph();
       //List<Set<String>> maxIndSets = this.getAllIndependentSetDepGraph(maxCliques);
         */


        /*
        this.optimizeVmAllocationCandidates(vmList);
        Log.printDebugMessages("@PVMAPMA @OptimizeAlloation ");
        ExecutionTimeMeasurer.start("optimizeAllocationTotal");

        // Find overutilized hosts
        ExecutionTimeMeasurer.start("optimizeAllocationHostSelection");
        List<PowerHostUtilizationHistory> overUtilizedHosts = getOverUtilizedHosts();
        getExecutionTimeHistoryHostSelection().add(
                ExecutionTimeMeasurer.end("optimizeAllocationHostSelection"));

        printOverUtilizedHosts(overUtilizedHosts);

        saveAllocation();

        // Find the VMs from overutilized hosts to migrate to new hosts

        ExecutionTimeMeasurer.start("optimizeAllocationVmSelection");
        List<? extends Vm> vmsToMigrate = getVmsToMigrateFromHosts(overUtilizedHosts);
        getExecutionTimeHistoryVmSelection().add(ExecutionTimeMeasurer.end("optimizeAllocationVmSelection"));

        Log.printLine("Reallocation of VMs from the over-utilized hosts:");
        ExecutionTimeMeasurer.start("optimizeAllocationVmReallocation");

        // Get Map, which VM will go to which new Hosts in data center. This method assigns to new host except from this overutilized hosts
        List<Map<String, Object>> migrationMap = getNewVmPlacement(vmsToMigrate, new HashSet<Host>(
                overUtilizedHosts)); // this second parameter acts as excluded hosts in called method
        getExecutionTimeHistoryVmReallocation().add(
                ExecutionTimeMeasurer.end("optimizeAllocationVmReallocation"));
        Log.printLine();

        // add new migration map for underutilized hosts
        migrationMap.addAll(getMigrationMapFromUnderUtilizedHosts(overUtilizedHosts));

        restoreAllocation();

        getExecutionTimeHistoryTotal().add(ExecutionTimeMeasurer.end("optimizeAllocationTotal"));
        */


        restoreAllocation();

        return selectedMigList;
    }

}
