#!/bin/bash
# Mark the current problem as complete on LLMeetCode
#
# This script calls the LLMeetCode API to mark the problem as completed.
# It reads configuration from the .llmeetcode-config file that was created
# when the codespace repository was generated.
#
# The config file contains:
#   LLMEETCODE_TOKEN      - Authentication token for the API
#   LLMEETCODE_API_URL    - Base URL of the LLMeetCode API
#   LLMEETCODE_PROBLEM_ID - Problem ID (for display purposes)

set -e

echo "========================================"
echo "  LLMeetCode - Mark Problem Complete"
echo "========================================"
echo ""

# Find the config file - check repo root first, then current directory
CONFIG_FILE=""
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

if [ -f "$REPO_ROOT/.llmeetcode-config" ]; then
    CONFIG_FILE="$REPO_ROOT/.llmeetcode-config"
elif [ -f ".llmeetcode-config" ]; then
    CONFIG_FILE=".llmeetcode-config"
elif [ -f "$HOME/workspace/.llmeetcode-config" ]; then
    # Fallback for codespaces where repo might be in /workspaces
    CONFIG_FILE="$HOME/workspace/.llmeetcode-config"
fi

# Load config from file if found (allows env vars to override)
if [ -n "$CONFIG_FILE" ] && [ -f "$CONFIG_FILE" ]; then
    echo "Loading configuration from $CONFIG_FILE"
    echo ""
    # Source the config file to set variables (only if not already set)
    while IFS='=' read -r key value; do
        # Skip comments and empty lines
        [[ "$key" =~ ^#.*$ ]] && continue
        [[ -z "$key" ]] && continue
        # Remove leading/trailing whitespace
        key=$(echo "$key" | xargs)
        value=$(echo "$value" | xargs)
        # Only set if not already defined in environment
        if [ -z "${!key}" ]; then
            export "$key=$value"
        fi
    done < "$CONFIG_FILE"
fi

# Check for required variables
if [ -z "$LLMEETCODE_TOKEN" ]; then
    echo "ERROR: LLMEETCODE_TOKEN is not set."
    echo ""
    echo "This script should be run from within a LLMeetCode codespace."
    echo "The .llmeetcode-config file should be in the repository root."
    echo ""
    echo "Looked for config file in:"
    echo "  - $REPO_ROOT/.llmeetcode-config"
    echo "  - ./.llmeetcode-config"
    exit 1
fi

if [ -z "$LLMEETCODE_API_URL" ]; then
    echo "ERROR: LLMEETCODE_API_URL is not set."
    echo ""
    echo "This script should be run from within a LLMeetCode codespace."
    exit 1
fi

# Display problem info if available
if [ -n "$LLMEETCODE_PROBLEM_ID" ]; then
    echo "Problem ID: $LLMEETCODE_PROBLEM_ID"
    echo ""
fi

echo "Calling LLMeetCode API to mark problem as complete..."
echo ""

# Make the API call
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST \
    "${LLMEETCODE_API_URL}/api/complete" \
    -H "Content-Type: application/json" \
    -d "{\"token\": \"${LLMEETCODE_TOKEN}\"}")

# Extract HTTP status code (last line) and response body
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 200 ]; then
    STATUS=$(echo "$BODY" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
    
    if [ "$STATUS" = "completed" ]; then
        echo "SUCCESS! Problem marked as complete."
        echo ""
        echo "Visit your LLMeetCode dashboard to see your progress."
    elif [ "$STATUS" = "already_completed" ]; then
        echo "This problem was already marked as complete."
    else
        echo "Response: $BODY"
    fi
elif [ "$HTTP_CODE" -eq 401 ]; then
    echo "ERROR: Authentication failed."
    echo "The token may have expired or is invalid."
    echo ""
    echo "Response: $BODY"
    exit 1
else
    echo "ERROR: API request failed with status $HTTP_CODE"
    echo "Response: $BODY"
    exit 1
fi

echo ""
echo "========================================"
