package com.theultimatelabs.mormontrivia.dao;

import static android.provider.BaseColumns._ID;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import au.com.bytecode.opencsv.CSVReader;

import com.theultimatelabs.mormontrivia.R;
import com.theultimatelabs.mormontrivia.constants.Constants;

/**
 * Data Access Object for the trivia database.
 * Handles initial table creation and population.
 * 
 * @author Dan Ruscoe
 */
public class TriviaDAO extends SQLiteOpenHelper
{
	private static final String DATABASE_NAME = "mormontriva.db";
	private static final int DATABASE_VERSION = 4;
	
	private static Context mContext;
	
	// Create table statements.
	
	private static final String CREATE_TABLE_USER_PREFS = "CREATE TABLE " + UserPrefsData.TABLE_NAME + " ("
	+ _ID + " INTEGER PRIMARY KEY, "
	+ UserPrefsData.SOUND + " INTEGER DEFAULT 1"
	+ ");";
	
	private static final String CREATE_TABLE_CATEGORIES = "CREATE TABLE "
	+ CategoryData.TABLE_NAME + " ("
	+ _ID + " INTEGER PRIMARY KEY, "
	+ CategoryData.NAME + " TEXT"
	+ ");";
	
	private static final String CREATE_TABLE_QUESTIONS = "CREATE TABLE "
	+ QuestionData.TABLE_NAME + " ("
	+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
	+ QuestionData.CATEGORY_ID + " INTEGER, "
	+ QuestionData.DIFFICULTY + " INTEGER, "
	+ QuestionData.TEXT + " TEXT,"
	+ QuestionData.DESCRIPTION + " TEXT"
	+ ");";
	
	private static final String CREATE_TABLE_ANSWERS = "CREATE TABLE "
	+ AnswerData.TABLE_NAME + " ("
	+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
	+ AnswerData.QUESTION_ID + " INTEGER, "
	+ AnswerData.TEXT + " TEXT,"
	+ AnswerData.CORRECT + " INTEGER"
	+ ");";
	
	private static final String CREATE_TABLE_SCORES = "CREATE TABLE "
	+ ScoreData.TABLE_NAME + " ("
	+ ScoreData.CATEGORY_ID + " INTEGER PRIMARY KEY, "
	+ ScoreData.QUESTIONS_ANSWERED + " INTEGER,"
	+ ScoreData.CORRECT_ANSWERS + " INTEGER"
	+ ");";
	
	/**
	 * Constructor for TriviaDAO.
	 * 
	 * @param Context context - The current application context.
	 */
	public TriviaDAO(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		
		mContext = context;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db)
	{
		// Create tables.
		
		Log.d(Constants.APP_LOG_NAME, "Creating database tables.");
		
		db.execSQL(CREATE_TABLE_USER_PREFS);
		db.execSQL(CREATE_TABLE_CATEGORIES);
		db.execSQL(CREATE_TABLE_QUESTIONS);
		db.execSQL(CREATE_TABLE_ANSWERS);
		db.execSQL(CREATE_TABLE_SCORES);
		
		// Populate tables.
		
		Log.d(Constants.APP_LOG_NAME, "Populating database tables");
		
		//importCategoriesData(db);
		
		importQuestionsData(db, R.raw.book_of_mormon,1);
		importQuestionsData(db, R.raw.joseph_smith,2);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		
		db.execSQL("DROP TABLE IF EXISTS " +UserPrefsData.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " +CategoryData.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " +QuestionData.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " +AnswerData.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " +ScoreData.TABLE_NAME);
		onCreate(db);
	}
	
	/**
	 * Imports trivia category data from the categories CSV file.
	 * 
	 * @param SQLiteDatabase db - The database connection.
	 */
	private void importCategoriesData(SQLiteDatabase db)
	{
		List<String[]> records = readDataFromFileResource(R.raw.categories_data);
		
		String[] recordData;
		
		for (int i = 0; i < records.size(); i++)
		{
			recordData = records.get(i);
			
			ContentValues values = new ContentValues();
			
			values.put(_ID, recordData[0]);
			values.put(CategoryData.NAME, recordData[1]);
			
			db.insert(CategoryData.TABLE_NAME, null, values);
		}
	}
	
	/**
	 * Imports trivia question data from a given CSV file resource ID.
	 * 
	 * @param SQLiteDatabase db - The database connection.
	 * @param int resourceId - The resource ID of the question data CSV file.
	 */
	private void importQuestionsData(SQLiteDatabase db, int resourceId, int categoryId)
	{
		List<String[]> records = readDataFromFileResource(resourceId);
		
		String[] recordData;
		
		long questionId = 0;
		
		ContentValues values = null;
		
		recordData = records.get(0);
		
		values = new ContentValues();
		
		values.put(_ID, categoryId);
		values.put(CategoryData.NAME, recordData[0]);
		
		db.insert(CategoryData.TABLE_NAME, null, values);
		
		for (int i = 1; i < records.size(); i++)
		{
			recordData = records.get(i);
			
			if (recordData.length < 5)
			{
				Log.e(Constants.APP_LOG_NAME, "Question record too short at index " + i + " while importing resource ID " + resourceId);
			}
			
			// Add the question data to the data base.
			
			values = new ContentValues();
			values.put(QuestionData.CATEGORY_ID, categoryId);
			values.put(QuestionData.DIFFICULTY, recordData[0]);
			values.put(QuestionData.TEXT, recordData[1]);
			values.put(QuestionData.DESCRIPTION, recordData[2]);
			
			questionId = db.insert(QuestionData.TABLE_NAME, _ID, values);
			
			
			for(int d=3; d<recordData.length; d++) {
				if(recordData[d].length()==0) {
					break;
				}
				else if(recordData[d].startsWith("*")) {
					importAnswerData(db, questionId, recordData[d].substring(1), true);
				}
				else {
					importAnswerData(db, questionId, recordData[d], false);
				}
				
			}
			//importAnswerData(db, questionId, recordData[5], false);
			//importAnswerData(db, questionId, recordData[6], false);
		}
	}
	
	/**
	 * Stores a question answer record in the database.
	 * Each question has multiple answers, with only one being correct.
	 * 
	 * @param SQLiteDatabase db - The database connection.
	 * @param long questionId - The ID of the question this answer maps to.
	 * @param String answerText - The text of the answer.
	 * @param boolean correct - True is the answer is the correct choice.
	 */
	private void importAnswerData(SQLiteDatabase db, long questionId, String answerText, boolean correct)
	{
		ContentValues values = new ContentValues();
		values.put(AnswerData.QUESTION_ID, questionId);
		values.put(AnswerData.TEXT, answerText);
		values.put(AnswerData.CORRECT, correct);
		
		db.insert(AnswerData.TABLE_NAME, _ID, values);
	}
	
	/**
	 * Reads rows from a CSV data file and returns each record
	 * as a string array.
	 * 
	 * @param int resourceId - The resource ID of the CSV data file.
	 * @return List<String[]> - The records from the data file.
	 */
	private List<String[]> readDataFromFileResource(int resourceId)
	{
		List<String[]> records = null;
		
		try
		{
			CSVReader reader = new CSVReader(new InputStreamReader(mContext.getResources().openRawResource(resourceId)));
			records = reader.readAll();
			reader.close();
		}
		catch (IOException e)
		{
			Log.e(Constants.APP_LOG_NAME, "Failed to read data from file: " + e.getMessage());
			e.printStackTrace();
		}
		
		return records;
	}
}