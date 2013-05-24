package com.theultimatelabs.mormontrivia.models;

/**
 * Represents a trivia category score.
 * 
 * @author Dan Ruscoe
 */
public class Score
{
	private int categoryId;
	private String categoryName;
	private int questionsAnswered;
	private int correctAnswers;
	
    @Override
    public String toString()
    {
        return "categoryId: " + categoryId + " categoryName: " + categoryName
        		+ " questionsAnswered: " + questionsAnswered + " correctAnswers: " + correctAnswers;
    }
    
    public int getKingdomIndex() {
    	int percent = (int)(((double)this.correctAnswers / (double)this.questionsAnswered) * 100);
    	if(percent == 0) {
    		return 0;
    	}
    	else if (percent<50) {
    		return 1;
    	}
    	else if (percent<90) {
    		return 2;
    	}
    	else if (percent<95) {
    		return 31;
    	}
    	else if (percent==100) {
    		return 33;
    	}
    	else {
    		return 32;
    	}
    }
    
    public String getKingdom(){
    	
    	switch(this.getKingdomIndex()) {
    	case 0:
    	default:
    		return "Outer Darkness";
    	case 1:
    		return "Telestrial Kingdom";
    	case 2:
    		return "Terrestrial Kingdom";
    	case 31:
    		return "1st Degree Celestrial Kingdom";
    	case 32:
    		return "2nd Degree Celestrial Kingdom";
    	case 33:
    		return "3rd Degree Celestrial Kingdom";
    	}
    
    }

    public String getKingdomScore() {
    	switch(this.getKingdomIndex()) {
    	case 0:
    	default:
    		return "You have denied the truth you know, you have been sent to Outer Darkness.";
    	case 1:
    		return "Good effort, you made to the Telestrial Kingdom...";
    	case 2:
    		return "Great Job, you made to the Terrestrial Kingdom!";
    	case 31:
    		return "Wow, you made to the 1st degree of the Celestrial Kingdom!";
    	case 32:
    		return "Almost perfect, you made to the 2nd degree of the Celestrial Kingdom!";
    	case 33:
    		return "PERFECT! You made to the 3rd degree of the Celestrial Kingdom!";
    	}
   
    }
    
    
	public int getCategoryId()
	{
		return categoryId;
	}

	public void setCategoryId(int categoryId)
	{
		this.categoryId = categoryId;
	}

	public String getCategoryName()
	{
		return categoryName;
	}

	public void setCategoryName(String categoryName)
	{
		this.categoryName = categoryName;
	}

	public int getQuestionsAnswered()
	{
		return questionsAnswered;
	}

	public void setQuestionsAnswered(int questionsAnswered)
	{
		this.questionsAnswered = questionsAnswered;
	}

	public int getCorrectAnswers()
	{
		return correctAnswers;
	}

	public void setCorrectAnswers(int correctAnswers)
	{
		this.correctAnswers = correctAnswers;
	}
}
