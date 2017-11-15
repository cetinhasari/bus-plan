package com.example;

import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.example.Plan;
import com.example.Student;


public class PlanScore implements EasyScoreCalculator<Plan> {

    public static final double costPerBusFixed = 20000.0;
    public static final double costPerUnitDistance = 0.5;
    public static final double bellTime = 36000; // in units of distance
    public static final double walkLimit = 1000;

    @Override
    public HardSoftScore calculateScore(Plan solution) {
	return calculateScore(solution, false);
    }

    public HardSoftScore calculateScore(Plan solution, Boolean verbose) {
	int totalDollars = 0;
	int delivered = 0;

	if (verbose) solution.display();

	for (Bus bus : solution.getBusList()) {
	    SourceOrSinkOrAnchor current;
	    List<Student> kids = new ArrayList<Student>();
	    double distance = 0.0;
	    int[] inFlow = {0, 0};
	    int[] outFlow = {0, 0};
	    int multiplicity = 0;
	    int routeDollars = 0;

	    bus.setMultiplicity(multiplicity);
	    if (bus.getNext() != null) {
		int[] capacity = bus.getWeights();
		current = bus;

		while (current != null) {
		    SourceOrSink next = current.getNext();

		    if (next != null) {
			double d = current.getNode().distance(next.getNode());
			distance += d;
			routeDollars += (int)(costPerUnitDistance * d);
		    }

		    if (current instanceof Stop) { // Stop
			Stop stop = (Stop)current;
			for (Student kid : stop.getStudentList()) {
			    if (kid.distance(stop) < walkLimit) {
				int[] weights = kid.getWeights();
				kids.add(kid);
				for (int i = 0; i < 2; ++i)
				    inFlow[i] += weights[i];
			    }
			}
		    }
		    else if (current instanceof School) { // School
			School school = (School)current;

			// Tally delivered kids
			if (distance < bellTime) {
			    for (Student kid : kids) {
				if (kid.getSchool().equals(school)) {
				    int[] weights = kid.getWeights();
				    for (int i = 0; i < 2; ++i) {
					outFlow[i] -= weights[i];
					delivered += weights[i];
				    }
				}
			    }
			}

			// Remove delivered kids from bus
			kids = kids
			    .stream()
			    .filter(kid -> !kid.getSchool().equals(school))
			    .collect(Collectors.toList());
		    }

		    for (int i = 0; i < 2; ++i) {
		    	int temp = (int)Math.ceil((double)(inFlow[i] - outFlow[i])/capacity[i]);
		    	multiplicity = Math.max(temp, multiplicity);
		    }

		    current = next;
		}
	    }
	    routeDollars += multiplicity * costPerBusFixed;
	    bus.setMultiplicity(multiplicity);
	    if (multiplicity > 0)
		totalDollars += routeDollars;
	}

	return HardSoftScore.valueOf(delivered - solution.getStudentList().size(), -totalDollars);
    }

}
