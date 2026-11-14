# Double Transposition: Encrypt, Decrypt & Break

A Java cryptanalysis project for the **Double Transposition cipher** (row and column permutation), now with a built-in web interface.

## Features
- **Encryption / Decryption** with user-defined row and column keys
- **Task #2: Brute force with known grid size.** Tries every row and column key pair (R! × C!)
- **Task #3: Brute force with unknown grid size.** Tests every valid grid size for the ciphertext length
- **Statistical scoring (0–100):** real-word coverage plus English letter-pair (bigram) analysis, so it breaks ordinary English text, not just a tiny word list
- **Multithreaded brute force** on all CPU cores, with live progress and a Stop button
- **Security analysis:** key space, entropy in bits, measured keys/second, time-to-break for different attackers, and a comparison with AES-128
- **Web interface** served by Java's built-in `HttpServer` (no external libraries)

## Run it
Requires JDK 11 or newer.

```bash
javac DoubleTransposition.java
java DoubleTransposition web        # then open http://localhost:8080
```

Or run `java DoubleTransposition` for the terminal menu and choose **6. Start Web Interface**.

## Deploy a public demo
The project includes a `Dockerfile`, so it runs on any host that supports Docker (for example Render, Railway or Fly.io).
The server reads the `PORT` environment variable automatically. Public-demo safeguards: at most 3 searches at once, request bodies are size-limited, and searches are capped at 10M key combinations.

## Project structure
```
DoubleTransposition.java   Cipher logic, brute-force attacks, web server
web/index.html             Website (calls the Java API)
Dockerfile                 For deploying the live demo
```

## Notes
- Grid sizes are limited to 1–9 per side because keys are written as single digits.
- Searches are capped at 10 million key combinations.
- A word-order model is not included yet, so a permutation that only reorders whole words can score the same as the original.
