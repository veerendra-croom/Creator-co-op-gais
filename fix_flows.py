import re
import os

def fix_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    # Pattern to match: val name by viewModel.method(arg1, arg2).collectAsState(initial = val)
    # We want to replace it with: val name by remember(arg1, arg2) { viewModel.method(arg1, arg2) }.collectAsState(initial = val)
    
    # Simple regex to match those lines
    pattern = r"val\s+(\w+)\s+by\s+([a-zA-Z0-9_]+ViewModel)\.([a-zA-Z0-9_]+)\(([^)]+)\)\.collectAsState\(([^)]*)\)"
    
    def replacer(match):
        var_name = match.group(1)
        vm_name = match.group(2)
        method_name = match.group(3)
        args = match.group(4)
        initial_val = match.group(5)
        
        return f"val {var_name} by remember({args}) {{ {vm_name}.{method_name}({args}) }}.collectAsState({initial_val})"
        
    new_content = re.sub(pattern, replacer, content)
    
    # Also handle the ones without initial value
    pattern2 = r"val\s+(\w+)\s+by\s+([a-zA-Z0-9_]+ViewModel)\.([a-zA-Z0-9_]+)\(([^)]+)\)\.collectAsState\(\)"
    
    def replacer2(match):
        var_name = match.group(1)
        vm_name = match.group(2)
        method_name = match.group(3)
        args = match.group(4)
        
        return f"val {var_name} by remember({args}) {{ {vm_name}.{method_name}({args}) }}.collectAsState()"

    new_content = re.sub(pattern2, replacer2, new_content)
    
    if new_content != content:
        with open(filepath, 'w') as f:
            f.write(new_content)
        print(f"Fixed {filepath}")

for root, dirs, files in os.walk("app/src/main/java/com/example/ui/screens"):
    for file in files:
        if file.endswith(".kt"):
            fix_file(os.path.join(root, file))

# And in CreatorCoOpDashboard.kt
fix_file("app/src/main/java/com/example/ui/CreatorCoOpDashboard.kt")

