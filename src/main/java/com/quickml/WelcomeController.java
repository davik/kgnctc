package com.quickml;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.quickml.pojos.Numbers;
import com.quickml.pojos.Student;



@Controller
public class WelcomeController {

	public WelcomeController(StudentRepository studRepo) {
		super();
		this.studRepo = studRepo;
	}

	// inject via application.properties
	@Value("${app.welcome.message}")
	private String MESSAGE = "";

	@Value("${app.welcome.title}")
	private String TITLE = "";
	
	@Autowired
	public final StudentRepository studRepo;

	@RequestMapping("/")
	public String welcome(Map<String, Object> model) {
		model.put("title", TITLE);
		model.put("message", MESSAGE);
		
		return "welcome";
	}

	// test 5xx errors
	@RequestMapping("/5xx")
	public String ServiceUnavailable() {
		throw new RuntimeException("ABC");
	}
	
	@RequestMapping(value = "/ml/average", method=RequestMethod.POST, consumes = "application/json")
    public Map<String, Object> getAverageML(@RequestBody Numbers numbers) {
    	Map<String, Object> statMap = new HashMap<>();
    	DescriptiveStatistics stats = new DescriptiveStatistics();
    	
    	List<Double> numbersz =  numbers.getNumbers();
    	    	
    	for(Double val: numbersz) {
    		stats.addValue(val);
    	}
    	
    	statMap.put("mean", stats.getMean());
    	statMap.put("geomean", stats.getGeometricMean());
    	statMap.put("max", stats.getMax());
    	statMap.put("min", stats.getMin());
    	statMap.put("skewness", stats.getSkewness());
    	statMap.put("kurtosis", stats.getKurtosis());
    	statMap.put("sd", stats.getStandardDeviation());
    	statMap.put("sum", stats.getSum());
    	statMap.put("var", stats.getVariance());
    	return statMap;
    }
	
	@RequestMapping(value = "/desc", method=RequestMethod.POST)
    String getDescStat(Map<String, Object> model,
    		@RequestBody Student student) throws IOException{
		System.out.println("Rest received");
		System.out.println(student.firstName);	
    	
    	model.put("firstName", student.firstName);
    	model.put("lastName", student.lastName);
    	studRepo.save(student);
    	return "desc";
    }
	
	@RequestMapping(value = "/summary", method=RequestMethod.GET)
    String getSummaryStat(Map<String, Object> model) throws IOException{
		System.out.println("Rest received");
		String numbers = "1, 2,4";
		System.out.println(numbers);
		
		numbers = numbers.replace(" ", "");
		Pattern pattern = Pattern.compile(",");
		List<Double> values = pattern.splitAsStream(numbers)
		                            .map(Double::valueOf)
		                            .collect(Collectors.toList());
		SummaryStatistics stats = new SummaryStatistics();
		for(Double val: values) {
    		stats.addValue(val);
    	}
    	
    	model.put("pop_var", stats.getPopulationVariance());
    	model.put("quadratic_mean", stats.getQuadraticMean());
    	model.put("second_moment", stats.getSecondMoment());
    	model.put("sum_log", stats.getSumOfLogs());
    	model.put("sum_square", stats.getSumsq());
    	return "summary";
    	
    }
	
	@RequestMapping(value = "/contact", method=RequestMethod.GET)
	String getContactPage(Map<String, Object> model) throws IOException {
		model.put("title", TITLE);
		model.put("message", MESSAGE);
		return "contact";
	}
	
	@RequestMapping(value = "/linear_algebra", method=RequestMethod.GET)
	String getLinearAlgebraPage(Map<String, Object> model) throws IOException {
		model.put("title", TITLE);
		model.put("message", MESSAGE);
		return "linear_algebra";
	}
	
	@RequestMapping(value = "/machine_learning", method=RequestMethod.GET)
	String getMachineLearningPage(Map<String, Object> model) throws IOException {
		model.put("title", TITLE);
		model.put("message", MESSAGE);
		return "machine_learning";
	}
	
	@RequestMapping(value = "/team", method=RequestMethod.GET)
	String getTeamPage(Map<String, Object> model) throws IOException {
		model.put("title", TITLE);
		model.put("message", MESSAGE);
		return "team";
	}
	
	@RequestMapping(value = "/statistics", method=RequestMethod.GET)
	String getStatisticsPage(Map<String, Object> model) throws IOException {
		model.put("message", MESSAGE);
		return "statistics";
	}

}