#!/bin/bash
# Mark the current problem as complete on LLMeetCode
#
# This script calls the LLMeetCode API to mark the problem as completed.
# It uses environment variables that were injected as repo secrets when
# the codespace was created.
#
# Required environment variables (injected automatically):
#   LLMEETCODE_TOKEN      - Authentication token for the API
#   LLMEETCODE_API_URL    - Base URL of the LLMeetCode API
#
# Optional:
#   LLMEETCODE_PROBLEM_ID - Problem ID (for display purposes)

set -e

echo "========================================"
echo "  LLMeetCode - Mark Problem Complete"
echo "========================================"
echo ""

# Check for required environment variables
if [ -z "$LLMEETCODE_TOKEN" ]; then
    echo "ERROR: LLMEETCODE_TOKEN environment variable is not set."
    echo ""
    echo "This script should be run from within a LLMeetCode codespace."
    echo "The token is automatically injected when the codespace is created."
    exit 1
fi

if [ -z "$LLMEETCODE_API_URL" ]; then
    echo "ERROR: LLMEETCODE_API_URL environment variable is not set."
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
