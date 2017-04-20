package nl.rug.ds.bpm.specification.jaxb;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * Created by p256867 on 6-4-2017.
 */

@XmlRootElement
public class Specification {
    private String id, type;
    private List<InputElement> inputElements;
    private SpecificationType specificationType;

    public Specification() {
        inputElements = new ArrayList<>();
    }

    public Specification(String type) {
        this();
        setType(type);
    }
    
    @XmlAttribute
    public void setId(String id) { this.id = id; }
    public String getId() { return id; }
    
    @XmlAttribute
    public void setType(String type) { this.type = type; }
    public String getType() { return type; }

    @XmlElementWrapper(name = "inputElements")
    @XmlElement(name = "inputElement")
    public List<InputElement> getInputElements() { return inputElements; }
    public void addInputElement(InputElement inputElement) { inputElements.add(inputElement); }

    public SpecificationType getSpecificationType() {
        return specificationType;
    }

    public void setSpecificationType(SpecificationType specificationType) {
        if(specificationType.getId().equals(type))
            this.specificationType = specificationType;
    }

    public List<String> getFormulas() {
        List<String> formulas = new ArrayList<>();

        if(specificationType != null) {
            for (Formula formula: specificationType.getFormulas()) {
                String f = formula.getFormula();

                for (Input input: specificationType.getInputs()) {
                    List<InputElement> elements = inputElements.stream().filter(element -> element.getTarget().equals(input.getValue())).collect(Collectors.toList());

                    String APBuilder = "";
                    if(elements.size() == 0) {
                        APBuilder = "true";
                    }
                    else if(elements.size() == 1) {
                        APBuilder = elements.get(0).getElement();
                    }
                    else {
                        Iterator<InputElement> inputElementIterator = elements.iterator();
                        APBuilder = inputElementIterator.next().getElement();
                        while (inputElementIterator.hasNext()) {
                            if (input.getType().equalsIgnoreCase("and"))
                                APBuilder = "(" + APBuilder + " & " + inputElementIterator.next().getElement() + ")";
                            else
                                APBuilder = "(" + APBuilder + " | " + inputElementIterator.next().getElement() + ")";
                        }
                    }
                    f = f.replaceAll(Matcher.quoteReplacement(input.getValue()), APBuilder.toString());
                }
                if(!f.equalsIgnoreCase(""))
                    formulas.add(formula.getLanguage() + " " + f);
            }
        }

        return formulas;
    }
}
