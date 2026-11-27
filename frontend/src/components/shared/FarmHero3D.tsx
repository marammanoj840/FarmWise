import { Suspense, useEffect, useMemo, useRef, useState } from "react";
import { Canvas, useFrame } from "@react-three/fiber";
import { Float, OrbitControls } from "@react-three/drei";
import * as THREE from "three";
import { CloudRain, Leaf, Sprout, SunMedium } from "lucide-react";

function detectWebGLSupport(): boolean {
  if (typeof window === "undefined" || typeof document === "undefined") return false;
  try {
    const canvas = document.createElement("canvas");
    const gl = canvas.getContext("webgl2") || canvas.getContext("webgl") || canvas.getContext("experimental-webgl");
    return Boolean(gl);
  } catch {
    return false;
  }
}

function FarmTerrainMesh({ reducedMotion }: { reducedMotion: boolean }) {
  const groupRef = useRef<THREE.Group>(null);
  const particlesRef = useRef<THREE.Points>(null);

  const plots = useMemo(() => {
    const grid: { x: number; z: number; height: number; color: string }[] = [];
    const palette = ["#1B5E3F", "#2A8C55", "#38A169", "#1E6B47", "#48B579", "#C88A24"];
    for (let ix = -2; ix <= 2; ix++) {
      for (let iz = -2; iz <= 2; iz++) {
        const idx = Math.abs(ix * 3 + iz * 5) % palette.length;
        const height = 0.22 + ((Math.abs(ix + iz) % 3) * 0.1);
        grid.push({ x: ix * 0.92, z: iz * 0.92, height, color: palette[idx] });
      }
    }
    return grid;
  }, []);

  const particlePositions = useMemo(() => {
    const count = 80;
    const positions = new Float32Array(count * 3);
    for (let i = 0; i < count; i++) {
      positions[i * 3] = ((i * 19) % 100) / 16 - 3;
      positions[i * 3 + 1] = ((i * 31) % 100) / 35 + 0.6;
      positions[i * 3 + 2] = ((i * 23) % 100) / 16 - 3;
    }
    return positions;
  }, []);

  useFrame((state, delta) => {
    if (reducedMotion) return;
    if (groupRef.current) {
      groupRef.current.rotation.y += delta * 0.11;
    }
    if (particlesRef.current) {
      particlesRef.current.position.y = Math.sin(state.clock.elapsedTime * 0.9) * 0.08;
    }
  });

  return (
    <group ref={groupRef} position={[0, -0.35, 0]}>
      {/* Soil base */}
      <mesh position={[0, -0.18, 0]} receiveShadow>
        <boxGeometry args={[5.1, 0.28, 5.1]} />
        <meshStandardMaterial color="#3A2E21" roughness={0.9} />
      </mesh>

      {/* Precision crop grid plots */}
      {plots.map((p, idx) => (
        <group key={idx} position={[p.x, p.height / 2, p.z]}>
          <mesh castShadow receiveShadow>
            <boxGeometry args={[0.78, p.height, 0.78]} />
            <meshStandardMaterial color={p.color} roughness={0.65} metalness={0.05} />
          </mesh>
          {/* Crop canopy cone */}
          <mesh position={[0, p.height / 2 + 0.16, 0]}>
            <coneGeometry args={[0.24, 0.34, 6]} />
            <meshStandardMaterial color="#4FD085" roughness={0.5} />
          </mesh>
        </group>
      ))}

      {/* Sensor mast */}
      <Float speed={1.5} floatIntensity={0.25}>
        <group position={[0, 1.15, 0]}>
          <mesh>
            <cylinderGeometry args={[0.04, 0.05, 1.35, 8]} />
            <meshStandardMaterial color="#D8E5DC" metalness={0.5} roughness={0.3} />
          </mesh>
          <mesh position={[0, 0.72, 0]}>
            <sphereGeometry args={[0.16, 16, 16]} />
            <meshStandardMaterial color="#EAB049" emissive="#C88A24" emissiveIntensity={0.45} />
          </mesh>
        </group>
      </Float>

      {/* Atmospheric moisture / telemetry particles */}
      <points ref={particlesRef}>
        <bufferGeometry>
          <bufferAttribute
            attach="attributes-position"
            args={[particlePositions, 3]}
          />
        </bufferGeometry>
        <pointsMaterial size={0.055} color="#7CE8A8" transparent opacity={0.75} />
      </points>
    </group>
  );
}

function WebGLFallbackVisual() {
  return (
    <div className="relative flex h-full w-full flex-col justify-between overflow-hidden rounded-3xl border border-[#C9D6CA] bg-gradient-to-br from-[#10402E] via-[#17573F] to-[#216E4E] p-6 text-white shadow-xl dark:border-[#244337]">
      <div className="flex items-center justify-between">
        <span className="inline-flex items-center gap-1.5 rounded-full bg-white/15 px-3 py-1 text-xs font-semibold backdrop-blur">
          <Sprout className="h-3.5 w-3.5 text-emerald-300" />
          Precision Plot Overview
        </span>
        <SunMedium className="h-6 w-6 text-amber-300" />
      </div>

      <svg viewBox="0 0 360 180" className="my-2 w-full" aria-hidden="true">
        <defs>
          <linearGradient id="hill1" x1="0" y1="0" x2="1" y2="1">
            <stop offset="0%" stopColor="#3FB973" stopOpacity="0.9" />
            <stop offset="100%" stopColor="#1B5E3F" stopOpacity="0.95" />
          </linearGradient>
        </defs>
        <path d="M0 140 Q90 85 180 120 T360 105 L360 180 L0 180 Z" fill="url(#hill1)" />
        <path d="M0 155 Q110 110 220 145 T360 130 L360 180 L0 180 Z" fill="#10402E" opacity="0.85" />
        <circle cx="290" cy="44" r="22" fill="#EAB049" opacity="0.85" />
      </svg>

      <div className="grid grid-cols-3 gap-2 text-xs">
        <div className="rounded-xl bg-white/10 p-2.5 backdrop-blur">
          <Leaf className="mb-1 h-4 w-4 text-emerald-300" />
          <p className="font-semibold">Crop Health</p>
          <p className="text-emerald-100/80">Stage Tracking</p>
        </div>
        <div className="rounded-xl bg-white/10 p-2.5 backdrop-blur">
          <CloudRain className="mb-1 h-4 w-4 text-sky-300" />
          <p className="font-semibold">Open-Meteo</p>
          <p className="text-emerald-100/80">Live Forecasts</p>
        </div>
        <div className="rounded-xl bg-white/10 p-2.5 backdrop-blur">
          <Sprout className="mb-1 h-4 w-4 text-amber-300" />
          <p className="font-semibold">Soil & NPK</p>
          <p className="text-emerald-100/80">Record Lab History</p>
        </div>
      </div>
    </div>
  );
}

export function FarmHero3D() {
  const [canUseWebGL] = useState<boolean>(() => detectWebGLSupport());
  const [reducedMotion, setReducedMotion] = useState<boolean>(
    () => (typeof window !== "undefined" ? window.matchMedia?.("(prefers-reduced-motion: reduce)").matches ?? false : false),
  );

  useEffect(() => {
    const mq = window.matchMedia?.("(prefers-reduced-motion: reduce)");
    if (!mq) return;
    const onChange = (e: MediaQueryListEvent) => setReducedMotion(e.matches);
    mq.addEventListener?.("change", onChange);
    return () => mq.removeEventListener?.("change", onChange);
  }, []);

  return (
    <div className="relative h-[340px] w-full sm:h-[420px]">
      {canUseWebGL ? (
        <div className="relative h-full w-full overflow-hidden rounded-3xl border border-[#C9D6CA] bg-gradient-to-b from-[#0E3B2B] via-[#134D38] to-[#0A261B] shadow-2xl dark:border-[#244337]">
          <Suspense fallback={<WebGLFallbackVisual />}>
            <Canvas
              dpr={[1, 1.5]}
              camera={{ position: [4.2, 3.3, 4.4], fov: 42 }}
              aria-label="Interactive 3D precision agriculture field visualization"
            >
              <ambientLight intensity={0.75} />
              <directionalLight position={[5, 8, 4]} intensity={1.35} color="#FFF6E0" />
              <pointLight position={[-4, 3, -3]} intensity={0.5} color="#6EE09B" />
              <FarmTerrainMesh reducedMotion={reducedMotion} />
              <OrbitControls
                enablePan={false}
                enableZoom={false}
                maxPolarAngle={Math.PI / 2.2}
                minPolarAngle={Math.PI / 4}
              />
            </Canvas>
          </Suspense>

          <div className="pointer-events-none absolute bottom-3 left-3 right-3 flex flex-wrap items-center justify-between gap-2 rounded-2xl border border-white/15 bg-black/35 px-3.5 py-2 text-xs text-white backdrop-blur-md">
            <span className="font-medium">Interactive 3D Precision Farm Grid — Drag to inspect plots</span>
            <span className="rounded-full bg-emerald-500/25 px-2.5 py-0.5 font-semibold text-emerald-200">
              WebGL Active
            </span>
          </div>
        </div>
      ) : (
        <WebGLFallbackVisual />
      )}
    </div>
  );
}
