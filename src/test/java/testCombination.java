import Infra.CandidateGKey;
import Infra.CandidateNode;
import Infra.CandidateType;
import org.paukov.combinatorics3.Generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class testCombination {

    public static void main(String []args)
    {
        ArrayList<CandidateNode> availableCandidates=new ArrayList<>();
        availableCandidates.add(new CandidateNode("type1",null, CandidateType.ConstantNode));
        availableCandidates.add(new CandidateNode("type2","edge1", CandidateType.VariableNode));
        availableCandidates.add(new CandidateNode("type3", null, CandidateType.ConstantNode));
        availableCandidates.add(new CandidateNode("type4","edge2", CandidateType.VariableNode));
        availableCandidates.add(new CandidateNode("type5","edge3", CandidateType.VariableNode));
        HashMap<Integer, List<CandidateGKey>> allCandidates=new HashMap<>();
        for (int i=1;i<=availableCandidates.size();i++)
        {
            allCandidates.put(i,new ArrayList<>());
            int finalI = i;
            Generator.combination(availableCandidates)
                    .simple(i)
                    .stream()
                    .forEach(elem -> allCandidates.get(finalI).add(new CandidateGKey("type",elem)));
        }
        System.out.println("Done.");
    }

}
