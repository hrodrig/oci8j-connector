#!/bin/bash

# Script to clean up .DS_Store files from the project
# Run this script if you want to remove any .DS_Store files that might exist

echo "🧹 Cleaning up .DS_Store files..."

# Find and remove .DS_Store files in the current directory and subdirectories
find . -name ".DS_Store" -type f -delete

# Count how many were removed
count=$(find . -name ".DS_Store" -type f | wc -l)

if [ $count -eq 0 ]; then
    echo "✅ No .DS_Store files found or all have been removed"
else
    echo "⚠️  Found $count .DS_Store files remaining"
fi

echo "🔍 Current .DS_Store files in project:"
find . -name ".DS_Store" -type f

echo ""
echo "💡 Tip: To prevent .DS_Store files in the future:"
echo "   - They are already in .gitignore"
echo "   - They are also in .gitattributes"
echo "   - Git will ignore them automatically"
