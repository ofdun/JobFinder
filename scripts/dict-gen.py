import os
schema = "jobfinder"

def gen_languages_sql():
    all_languages = ["English", "Spanish", "French", "German", "Chinese", "Japanese", "Russian", "Portuguese", "Arabic", "Hindi"]
    proficiency = ["A1", "A2", "B1", "B2", "C1", "C2"]
    res = f"INSERT INTO {schema}.languages (name, proficiency_level) VALUES\n"
    for lang in all_languages:
        for prof in proficiency:
            res += f"('{lang}', '{prof}'),\n"
    res = res[:-2] + ";\n"
    return res

_country_map = None

def _load_country_map():
    global _country_map
    if _country_map is not None:
        return _country_map
    _country_map = {}
    script_dir = os.path.dirname(__file__)
    path = os.path.join(script_dir, 'countryInfo.txt')
    if not os.path.exists(path):
        return _country_map
    with open(path, 'r', encoding='utf-8', errors='replace') as fh:
        for line in fh:
            line = line.strip('\n')
            if not line or line.startswith('#'):
                continue
            parts = line.split('\t')
            if len(parts) < 5:
                continue
            code = parts[0].strip().upper()
            name = parts[4].strip()
            if len(code) == 2 and name:
                _country_map[code] = name
    return _country_map

def get_country_name(code: str) -> str:
    if not code:
        return code
    code = code.strip().upper()
    cmap = _load_country_map()
    return cmap.get(code, code)

def gen_locations_sql():
    script_dir = os.path.dirname(__file__)
    cities_path = os.path.join(script_dir, 'cities500.txt')
    if not os.path.exists(cities_path):
        exit(f"Error: {cities_path} not found. Please download it from GeoNames and place it in the same directory as this script.")
    seen = set()
    entries = []
    with open(cities_path, 'r', encoding='utf-8', errors='replace') as fh:
        for line in fh:
            line = line.strip('\n')
            if not line:
                continue
            parts = line.split('\t')
            if len(parts) < 9:
                parts = line.split()
                if len(parts) < 9:
                    continue
            city = parts[1].strip()
            country_code = parts[8].strip()
            if not city or not country_code:
                continue
            country_name = get_country_name(country_code)
            key = (city.lower(), country_name.lower())
            if key in seen:
                continue
            seen.add(key)
            city_sql = city.replace("'", "''")
            country_sql = country_name.replace("'", "''")
            entries.append((city_sql, country_sql))
    if not entries:
        exit("Error: No valid city-country entries found in cities500.txt.")
    res = f"INSERT INTO {schema}.locations (city, country) VALUES\n"
    for city_sql, country_sql in entries:
        res += f"('{city_sql}', '{country_sql}'),\n"
    res = res[:-2] + ";\n"
    return res

def gen_skills_sql():
    skills = [
        "Python", "Java", "C++", "JavaScript", "SQL", "AWS", "Docker", "Kubernetes",
        "React", "Angular", "Node.js", "Django", "Flask", "Spring Boot",
        "Git", "Linux", "Agile Methodologies", "Machine Learning",
        "Data Analysis", "Communication Skills", "Problem Solving",
        "Project Management", "Time Management", "Teamwork", "Leadership",
        "Critical Thinking", "Adaptability", "Creativity", "Attention to Detail",
        "Customer Service", "Sales", "Marketing", "Financial Analysis",
        "Graphic Design", "UI/UX Design", "Content Creation", "Copywriting",
        "Public Speaking", "Negotiation", "Conflict Resolution", "Emotional Intelligence",
        "Foreign Languages", "Research Skills", "Analytical Skills", "Organizational Skills",
        "Interpersonal Skills", "Collaboration", "Decision Making", "Multitasking",
        "Networking", "Presentation Skills", "Strategic Planning", "Business Acumen",
        "Data Visualization", "Cloud Computing", "Cybersecurity", "Mobile Development",
        "DevOps", "Big Data", "Artificial Intelligence", "Blockchain",
        "Internet of Things", "Virtual Reality", "Augmented Reality", "Quantum Computing",
        "Robotics", "3D Printing", "Biotechnology", "Nanotechnology",
        "Renewable Energy", "Sustainability", "Environmental Science", "Healthcare",
        "Education", "Social Work", "Nonprofit Management", "Public Policy",
        "Law", "Accounting", "Human Resources", "Supply Chain Management",
        "Logistics", "Manufacturing", "Construction", "Real Estate",
        "Transportation", "Hospitality", "Tourism", "Entertainment",
        "Sports", "Fitness", "Wellness", "Food Service",
        "Retail", "E-commerce", "Customer Support", "Technical Support",
        "Data Entry", "Administrative Assistance", "Virtual Assistance", "Personal Assistance",
        "Event Planning", "Fundraising", "Grant Writing", "Volunteer Coordination",
        "Social Media Management", "Community Management", "Public Relations", "Brand Management",
        "Advertising", "Market Research", "Product Management", "Business Development",
        "Entrepreneurship", "Innovation", "Change Management", "Risk Management",
        "Compliance", "Quality Assurance", "Testing", "Debugging",
        "Version Control", "Continuous Integration", "Continuous Deployment", "Monitoring",
        "Performance Optimization", "Scalability", "Reliability", "Security",
        "Accessibility", "Internationalization", "Localization", "Documentation",
        "Training", "Mentoring", "Coaching", "Team Building",
        "Conflict Management", "Stress Management", "Work-Life Balance", "Emotional Resilience",
        "Cultural Competence", "Diversity and Inclusion", "Ethical Decision Making",
        "Corporate Social Responsibility", "Sustainability Practices", "Global Awareness", "Community Engagement",
    ]
    res = f"INSERT INTO {schema}.skills (name) VALUES\n"
    for skill in skills:
        skill_sql = skill.replace("'", "''")
        res += f"('{skill_sql}'),\n"
    res = res[:-2] + ";\n"

    return res

def gen_categories_sql():
    category = [
        "Software Development", "Data Science", "Project Management", "Marketing",
        "Sales", "Customer Service", "Design", "Finance",
        "Healthcare", "Education", "Human Resources", "Operations",
        "Legal", "Manufacturing", "Construction", "Transportation",
        "Hospitality", "Retail", "Entertainment", "Sports",
        "Fitness", "Wellness", "Food Service", "E-commerce",
        "Nonprofit", "Public Policy", "Environmental Science",
        "Biotechnology", "Renewable Energy", "Artificial Intelligence",
        "Cybersecurity", "Cloud Computing", "Mobile Development",
        "DevOps", "Big Data", "Blockchain", "Internet of Things",
        "Virtual Reality", "Augmented Reality", "Quantum Computing",
        "Robotics", "3D Printing"
    ]

    res = f"INSERT INTO {schema}.categories (name) VALUES\n"
    for cat in category:
        cat_sql = cat.replace("'", "''")
        res += f"('{cat_sql}'),\n"
    res = res[:-2] + ";\n"
    return res

def write_to_file(filename, content):
    with open(filename, 'w', encoding='utf-8') as f:
        f.write(content)

def main():
    res = ""
    res += gen_languages_sql()
    res += gen_locations_sql()
    res += gen_skills_sql()
    res += gen_categories_sql()
    write_to_file("../JobFinder/src/main/resources/db/migration/V3__add_dicts.sql", res)

if __name__ == "__main__":
    main()