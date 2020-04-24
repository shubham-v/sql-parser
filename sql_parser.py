from moz_sql_parser import parse
import json
import argparse

def f(sql):
    json_format = json.dumps(parse(sql))
    print(json_format)
    return

def main():
    parser = argparse.ArgumentParser(description='The sql')
    parser.add_argument('--sql', help='The sql')
    args = parser.parse_args()
    sql = args.sql
    f(sql)

if __name__ == '__main__':
    main()