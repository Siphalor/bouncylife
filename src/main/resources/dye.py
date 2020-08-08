import sys, os;

colors = (
        "red",
        "orange",
        "brown",
        "yellow",
        "white",
        "gray",
        "light_gray",
        "black",
        "lime",
        "green",
        "cyan",
        "light_blue",
        "blue",
        "pink",
        "magenta",
        "purple"
        )

def main():
    if len(sys.argv) < 2:
        print('Not enough args!');
        sys.exit(100)

    sys.argv.pop(0)

    for template in sys.argv:
        with open(template, 'r') as t_file:
            t_file = t_file.read()
            t_line = t_file.splitlines()[0]
            (out_dir, base_name) = os.path.split(template)
            base_name = '%color%_' + base_name
            if t_line[0] == '$':
                base_name = t_line[1:]
                t_file = '\n'.join(t_file.splitlines()[1:])
            for color in colors:
                out_text = t_file.replace('%color%', color)
                out_path = os.path.join(out_dir, base_name.replace('%color%', color))
                print(out_path)
                with open(out_path, 'w') as out_file:
                    out_file.write(out_text)

if __name__ == '__main__': main()
