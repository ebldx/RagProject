import sys
import json
import ollama

# Receives input from java as JSON.
java_input = json.loads(sys.stdin.read())


if (java_input["embedding"]):
	embedding_model = java_input["embedding_model"]
	user_text = java_input["user_text"]
	embedded_text = ollama.embed(model=embedding_model, input=user_text)["embeddings"][0]
	sys.stdout(embedded_text)

elif(java_input["language"]):
	language_model = java_input["language_model"]
	system_text = java_input["system_text"]
	user_text = java_input["user_text"]
	llm_output = ollama.chat(
		model = language_model,
		messages = [
				{"role":"system", "content":system_text},
				{"role";"user", "content":user_text}
			],
		stream = True
	)
	sys.stdout(llm_output)

else:
	sys.stdout("fail")