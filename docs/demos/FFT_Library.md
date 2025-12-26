# FFT Library Documentation v2.0.0-SNAPSHOT

## Struttura File

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── fft/
│   │           ├── core/                   # Interfacce core e implementazione base
│   │           │   ├── FFT.java            # Interfaccia principale
│   │           │   ├── FFTBase.java        # Implementazione generica di riferimento
│   │           │   └── FFTResult.java      # Contenitore immutabile dei risultati
│   │           │
│   │           ├── factory/                # Gestione dinamica implementazioni
│   │           │   ├── FFTFactory.java     # Interfaccia factory  
│   │           │   ├── DefaultFFTFactory.java # Selezione basata su priorità  
│   │           │   ├── FFTImplementation.java # Metadati annotazione
│   │           │   └── FFTImplementationDiscovery.java # Auto-registrazione
│   │           │
│   │           ├── optimized/              # 14 implementazioni specifiche per dimensione
│   │           │   ├── FFTOptimized8.java  # 8-point (1.42-2.45x speedup verificato)
│   │           │   ├── FFTOptimized16.java # 16-point (fallback implementation)
│   │           │   ├── FFTOptimized32.java # 32-point (1.33-1.56x speedup verificato)
│   │           │   ├── FFTOptimized64.java # 64-point (fallback implementation)
│   │           │   ├── ... (sizes 128-65536) # Tutte le potenze di 2 fino a 65536
│   │           │   └── OptimizedFFTUtils.java # Logica ottimizzata condivisa
│   │           │
│   │           ├── demo/                   # Applicazioni dimostrative avanzate
│   │           │   ├── PitchDetectionDemo.java # Rilevamento tonalità in tempo reale
│   │           │   ├── SongRecognitionDemo.java # Riconoscimento melodie (Parsons code)
│   │           │   ├── SimulatedPitchDetectionDemo.java # Validazione performance
│   │           │   └── ParsonsCodeUtils.java # Utilità per music information retrieval
│   │           │
│   │           └── utils/                  # Utilità deprecate
│   │               └── FFTUtils.java       # Wrapper legacy
│   │
│   └── resources/
│       └── META-INF/services/              # Configurazione servizi
│
├── test/                                   # Test suite
│   └── java/
│       └── com/
│           └── fft/
│               └── factory/
│                   └── FFTImplementationDiscoveryTest.java
└── target/                                 # Output compilato (ignorato)
```

## Componenti Principali

### Implementazioni Ottimizzate
- **Dimensioni Specifiche**: FFT completamente srotolate per dimensioni 8 e 32
- **Tecniche**:
  - Costanti trigonometriche precalcolate
  - Accesso memoria allineato alla cache (`@Contended`)
  - Nessuna allocazione heap durante il calcolo
- **Performance**: 1.4-1.8x più veloce di FFTBase generico

```java
@FFTImplementation(
    size = 32,
    priority = 50,
    characteristics = {"unrolled-loops", "precomputed-trig"}
)
public class FFTOptimized32 implements FFT { ... }
```

### Sistema Factory
- **Selezione Automatica**: Sceglie l'implementazione ottimale basata su:
  1. Corrispondenza esatta della dimensione
  2. Priorità più alta 
  3. Fallback su FFTBase

### Novità in v4.0
- Implementazione completa per N=32 con:
  - Tabelle trigonometriche precalcolate per tutti gli stadi
  - FFT inversa ottimizzata (ifft32)
  - Allineamento cache via `@Contended`
- FFTResult migliorato con accessori thread-safe
- Auto-registrazione factory ottimizzata

## Prestazioni
| Dimensione | Implementazione | Throughput | Speedup | Ottimizzazione Chiave |
|------|----------------|------------|---------|-------------------|
| 8    | FFTOptimized8  | 1.48M ops/s | 1.4x    | Srotolamento completo | 
| 32   | FFTOptimized32 | 710K ops/s | 1.8x    | Calcolo a stadi |
| 64   | FFTBase        | 190K ops/s | 1.0x    | Fallback generico |

## Guida alla Migrazione
### Aggiornamento da v3.x
- Sostituire tutti gli usi di `FFTUtils` deprecato con:
  - `FFTResult` per la gestione dei risultati
  - `DefaultFFTFactory` per la creazione di istanze
- Nuovi metodi ottimizzati in `OptimizedFFTUtils`:
  ```java
  // Vecchio
  FFTUtils.fft(input);
  // Nuovo
  factory.createFFT(32).transform(input);
  ```

## Metodi di Utilità Aggiuntivi

Il pacchetto `FFTUtils` fornisce varie funzioni di supporto per analizzare le
implementazioni disponibili e preparare i segnali:

```java
// Creazione manuale di una factory e report del registro
FFTFactory factory = FFTUtils.createFactory();
System.out.println(factory.getRegistryReport());

// Informazioni sull'implementazione scelta per una certa dimensione
String info = FFTUtils.getImplementationInfo(1024);

// Calcolo della prossima potenza di due e zero padding
int next = FFTUtils.nextPowerOfTwo(300); // 512
double[] padded = FFTUtils.zeroPadToPowerOfTwo(signal);
```

L'utilità `getSupportedSizes()` restituisce l'elenco di tutte le dimensioni
gestite dalla factory predefinita.
