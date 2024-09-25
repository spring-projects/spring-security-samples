#!/bin/bash

# Root gradle directory containing the original libs.versions.toml
root_gradle_dir="gradle"
root_libs_versions_file="${root_gradle_dir}/libs.versions.toml"

# Check if the root libs.versions.toml file exists
if [ ! -f "$root_libs_versions_file" ]; then
  echo "Root libs.versions.toml file not found in ${root_gradle_dir}. Exiting."
  exit 1
fi

# Find all subdirectories named "gradle"
find . -type d -name "gradle" | while read -r gradle_dir; do
  # Skip the root gradle directory
  if [ "$gradle_dir" != "./$root_gradle_dir" ]; then
    # Target file in the subdirectory
    target_file="${gradle_dir}/libs.versions.toml"
    
    # Remove the target file if it already exists
    if [ -f "$target_file" ] || [ -L "$target_file" ]; then
      rm -f "$target_file"
    fi
    
    # Calculate the relative path from the subdirectory to the root libs.versions.toml
    relative_path=$(realpath --relative-to="$gradle_dir" "$root_libs_versions_file")
    
    # Create a symbolic link
    ln -s "$relative_path" "$target_file"
    echo "Created symlink: ${target_file} -> ${relative_path}"
  fi
done
