#!/usr/bin/env node
import { execSync } from "node:child_process";
import { existsSync, readFileSync } from "node:fs";
import { extname } from "node:path";
import { TextDecoder } from "node:util";

const UTF8_FATAL_DECODER = new TextDecoder("utf-8", { fatal: true });
const UTF8_BOM = [0xef, 0xbb, 0xbf];

const TEXT_EXTENSIONS = new Set([
  ".java",
  ".kt",
  ".kts",
  ".xml",
  ".yml",
  ".yaml",
  ".properties",
  ".sql",
  ".js",
  ".mjs",
  ".cjs",
  ".ts",
  ".tsx",
  ".jsx",
  ".vue",
  ".css",
  ".scss",
  ".less",
  ".html",
  ".md",
  ".txt",
  ".json",
  ".gitignore",
  ".gitattributes",
  ".editorconfig"
]);

const MOJIBAKE_MARKERS = [
  "\u7487",
  "\u9359",
  "\u7f01",
  "\u95bf",
  "\u95c2",
  "\u9428",
  "\u8930",
  "\u68f0",
  "\u93b4",
  "\u951b",
  "\u92c6",
  "\u95ab",
  "\u7c2c"
];
const MOJIBAKE_PATTERN = new RegExp(MOJIBAKE_MARKERS.join("|"));

function exec(command) {
  return execSync(command, { encoding: "utf8" }).trim();
}

function stagedFiles() {
  const output = exec("git diff --cached --name-only --diff-filter=ACM");
  if (!output) {
    return [];
  }
  return output.split(/\r?\n/).filter(Boolean);
}

function isTextCandidate(filePath) {
  const ext = extname(filePath).toLowerCase();
  if (TEXT_EXTENSIONS.has(ext)) {
    return true;
  }
  const fileName = filePath.replace(/\\/g, "/").split("/").pop() ?? "";
  return TEXT_EXTENSIONS.has(fileName);
}

function validateUtf8(filePath) {
  const bytes = readFileSync(filePath);
  if (
    bytes.length >= 3 &&
    bytes[0] === UTF8_BOM[0] &&
    bytes[1] === UTF8_BOM[1] &&
    bytes[2] === UTF8_BOM[2]
  ) {
    throw new Error("contains UTF-8 BOM (\\uFEFF)");
  }
  const text = UTF8_FATAL_DECODER.decode(bytes);
  if (text.includes("\uFFFD")) {
    throw new Error("contains replacement character U+FFFD");
  }
  return text;
}

function main() {
  const providedFiles = process.argv.slice(2);
  const files = (providedFiles.length > 0 ? providedFiles : stagedFiles())
    .filter((filePath) => existsSync(filePath))
    .filter(isTextCandidate);

  if (files.length === 0) {
    process.exit(0);
  }

  const errors = [];
  const warnings = [];

  for (const filePath of files) {
    try {
      const text = validateUtf8(filePath);
      if (MOJIBAKE_PATTERN.test(text)) {
        warnings.push(`${filePath}: suspicious mojibake pattern found`);
      }
    } catch (error) {
      errors.push(`${filePath}: ${error.message}`);
    }
  }

  if (warnings.length > 0) {
    console.warn("Encoding warnings:");
    for (const warning of warnings) {
      console.warn(`  - ${warning}`);
    }
    console.warn("If this is intentional, review manually before commit.");
  }

  if (errors.length > 0) {
    console.error("Encoding check failed:");
    for (const error of errors) {
      console.error(`  - ${error}`);
    }
    process.exit(1);
  }
}

main();
