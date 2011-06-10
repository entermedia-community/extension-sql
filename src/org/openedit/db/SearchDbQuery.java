package org.openedit.db;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.openedit.hittracker.SearchQuery;
import com.openedit.hittracker.Term;

public class SearchDbQuery extends SearchQuery
{
	
	public Term addMatches(String inField, String inVal)
	{
		Term term = new Term(){
			public String toQuery(){
				return getId() + "='" + getValue() + "'";
			}
			public Element toXml()
			{	
				Element term = DocumentHelper.createElement("term");
				term.addAttribute("id", getId());
				term.addAttribute("val", getValue());
				term.addAttribute("op", "matches");
				
				return term;
			}

		};
		term.setId(inField);
		term.setValue(inVal);
		getTerms().add(term);
		return term;
	}
	
	public String toQuery()
	{
		
		StringBuffer done = new StringBuffer();
		String op = null;
		if( isAndTogether())
		{
			op = " = ";
		}
		else
		{
			done.append("(");
			op = " = ";
		}
		for (int i = 0; i < fieldTerms.size(); i++)
		{
			Term field= (Term)fieldTerms.get(i);
			String q = field.toQuery();
			if( i > 0 && !q.startsWith("+") && !q.startsWith("-"))
			{
				done.append(op);
			}
			done.append(q);
			if( i+1 < fieldTerms.size() )
			{
				done.append(" ");
			}
		}
		if( !isAndTogether() )
		{
			done.append(")");
		}
		return done.toString();
	}
}