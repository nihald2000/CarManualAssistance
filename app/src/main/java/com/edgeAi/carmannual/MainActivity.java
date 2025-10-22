package com.edgeAi.carmannual;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.mediapipe.tasks.genai.llminference.LlmInference;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private EditText questionInput;
    private Button askButton;
    private TextView responseText;
    private ProgressBar progressBar;

    private LlmInference llmInference;
    private ExecutorService executorService;
    private boolean isModelLoaded = false;

    // Model path - change this to where you push your .task file
    private static final String MODEL_PATH = "/data/local/tmp/llm/car_manual_model.task";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        questionInput = findViewById(R.id.questionInput);
        askButton = findViewById(R.id.askButton);
        responseText = findViewById(R.id.responseText);
        progressBar = findViewById(R.id.progressBar);

        executorService = Executors.newSingleThreadExecutor();

        // Load model on startup
        loadModel();

        askButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String question = questionInput.getText().toString().trim();
                if (question.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter a question", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!isModelLoaded) {
                    Toast.makeText(MainActivity.this, "Model is still loading...", Toast.LENGTH_SHORT).show();
                    return;
                }
                askQuestion(question);
            }
        });
    }

    private void loadModel() {
        progressBar.setVisibility(View.VISIBLE);
        responseText.setText("Loading Car Manual AI model...\n\nNote: Make sure you've pushed the .task file to:\n" + MODEL_PATH);
        askButton.setEnabled(false);

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // Check if model file exists
                    File modelFile = new File(MODEL_PATH);
                    if (!modelFile.exists()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                responseText.setText("❌ Model file not found!\n\n" +
                                        "Please push your .task file using:\n\n" +
                                        "adb shell mkdir -p /data/local/tmp/llm/\n" +
                                        "adb push car_manual_model.task /data/local/tmp/llm/\n\n" +
                                        "Current path: " + MODEL_PATH);
                                progressBar.setVisibility(View.GONE);
                            }
                        });
                        return;
                    }

                    // Configure LLM Inference options for .task file
                    LlmInference.LlmInferenceOptions options =
                            LlmInference.LlmInferenceOptions.builder()
                                    .setModelPath(MODEL_PATH)
                                    .setMaxTokens(512)
                                    .setMaxTopK(40)
                                    .build();

                    // Create LLM Inference instance
                    llmInference = LlmInference.createFromOptions(
                            MainActivity.this,
                            options
                    );

                    isModelLoaded = true;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            responseText.setText("✅ Car Manual AI Model loaded successfully!\n\n" +
                                    "You can now ask questions about:\n" +
                                    "• Car maintenance\n" +
                                    "• Parts and components\n" +
                                    "• Troubleshooting\n" +
                                    "• General car information\n\n" +
                                    "Type your question below!");
                            askButton.setEnabled(true);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    final String errorMsg = e.getMessage();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            responseText.setText("❌ Error loading model:\n\n" + errorMsg +
                                    "\n\nMake sure:\n" +
                                    "1. The .task file is valid\n" +
                                    "2. File path is correct\n" +
                                    "3. Device has enough memory");
                        }
                    });
                }
            }
        });
    }

    private void askQuestion(final String question) {
        progressBar.setVisibility(View.VISIBLE);
        responseText.setText("🤔 Thinking...");
        askButton.setEnabled(false);

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // Generate response using the model
                    String response = llmInference.generateResponse(question);

                    final String finalResponse = response;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            responseText.setText("Q: " + question + "\n\n" +
                                    "A: " + finalResponse);
                            askButton.setEnabled(true);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    final String errorMsg = e.getMessage();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            responseText.setText("❌ Error generating response:\n\n" + errorMsg);
                            askButton.setEnabled(true);
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up resources
        if (llmInference != null) {
            llmInference.close();
        }
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}