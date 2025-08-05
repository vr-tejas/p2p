# Shared Files Directory

## Purpose
This directory contains files that you want to share with other peers in the P2P network.

## How to Use
1. **Place files here** that you want other peers to download
2. **Start your peer** with `java Peer <port>`
3. **Other peers can connect** and see these files in their file list
4. **Files are served automatically** when other peers request them

## Supported File Types
- Text files (`.txt`, `.md`, `.csv`)
- Documents (`.pdf`, `.doc`, `.docx`)
- Images (`.jpg`, `.png`, `.gif`)
- Any file type (the system handles binary data)

## Example Files
The application will automatically create sample files when you first run it if this directory is empty.

## Important Notes
- Files in this directory are **publicly accessible** to connected peers
- Make sure you only share files you want others to access
- Large files will take longer to transfer
- File names should not contain special characters for best compatibility